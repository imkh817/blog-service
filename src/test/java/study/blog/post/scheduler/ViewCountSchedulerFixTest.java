package study.blog.post.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.CollectionUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import study.blog.post.domain.PostStatus;
import study.blog.post.domain.entity.Post;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;
import study.blog.post.infrastructure.scheduler.ViewCountScheduler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * [수정 후 검증] ViewCountFlusher 분리 후 트랜잭션 격리 검증
 *
 * 수정 전: @Transactional이 루프 전체 → 한 포스트 실패 시 전체 롤백
 * 수정 후: 트랜잭션 범위를 포스트 1건 단위로 격리 → 실패 범위가 해당 포스트로 한정
 */
@SpringBootTest
@Testcontainers
class ViewCountSchedulerFixTest {

    static final MySQLContainer<?> MYSQL_CONTAINER;
    static final GenericContainer<?> REDIS_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("test_db")
                .withUsername("root")
                .withPassword("test");
        MYSQL_CONTAINER.start();

        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
                .withExposedPorts(6379);
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS_CONTAINER.getMappedPort(6379)));
    }

    @SpyBean
    private PostCommandRepository postCommandRepository;

    @SpyBean
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ViewCountScheduler viewCountScheduler;

    @AfterEach
    void cleanup() {
        postCommandRepository.deleteAll();
        Set<String> keys = redisTemplate.keys("post:view:count:*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        reset(redisTemplate);
    }

    @Nested
    @DisplayName("수정 후 검증: 트랜잭션 격리")
    class AfterFix {

        @Test
        @DisplayName("post2 DB 실패 + Redis 복원 실패 시 post1의 조회수는 정상 반영된다")
        void post2_장애_발생_시_post1_조회수_정상_반영() {
            // given
            Post post1 = postCommandRepository.save(
                    Post.createPost(1L, "포스트1", "내용1", PostStatus.PUBLISHED, List.of("Java"), "thumb.jpg", List.of())
            );
            Post post2 = postCommandRepository.save(
                    Post.createPost(1L, "포스트2", "내용2", PostStatus.PUBLISHED, List.of("Java"), "thumb.jpg", List.of())
            );

            String key1 = "post:view:count:" + post1.getId();
            String key2 = "post:view:count:" + post2.getId();

            // post1: 5명 조회, post2: 3명 조회
            redisTemplate.opsForValue().set(key1, "5");
            redisTemplate.opsForValue().set(key2, "3");

            // 처리 순서 고정: post1 먼저, post2 나중
            doReturn(new LinkedHashSet<>(List.of(key1, key2)))
                    .when(redisTemplate).keys("post:view:count:*");

            // post2만 DB + Redis 장애
            doThrow(new RuntimeException("post2 DB 장애"))
                    .when(postCommandRepository).incrementViewCount(eq(post2.getId()), anyLong());

            @SuppressWarnings("unchecked")
            ValueOperations<String, String> spyOps = spy(redisTemplate.opsForValue());
            doReturn(spyOps).when(redisTemplate).opsForValue();
            doThrow(new RuntimeException("post2 Redis 복원 실패"))
                    .when(spyOps).increment(eq(key2), anyLong());

            // when - 수정 후에는 예외가 전파되지 않음
            viewCountScheduler.flushViewCountToDB();

            reset(redisTemplate);

            // then
            // post1: 정상 커밋 (수정 전에는 0으로 롤백됐던 부분)
            Post updatedPost1 = postCommandRepository.findById(post1.getId()).orElseThrow();
            assertThat(updatedPost1.getViewCount())
                    .as("post1은 post2와 독립 트랜잭션 → 정상 커밋")
                    .isEqualTo(5L);

            // post2: DB 실패로 0 (해당 건만 소실)
            Post updatedPost2 = postCommandRepository.findById(post2.getId()).orElseThrow();
            assertThat(updatedPost2.getViewCount())
                    .as("post2는 DB 실패 → 0 (해당 건만 영향)")
                    .isEqualTo(0L);
        }

        @Test
        @DisplayName("정상 케이스: 모든 포스트의 조회수가 DB에 반영된다")
        void 정상_케이스_모든_조회수_반영() {
            // given
            Post post1 = postCommandRepository.save(
                    Post.createPost(1L, "포스트1", "내용1", PostStatus.PUBLISHED, List.of("Java"), "thumb.jpg", List.of())
            );
            Post post2 = postCommandRepository.save(
                    Post.createPost(1L, "포스트2", "내용2", PostStatus.PUBLISHED, List.of("Java"), "thumb.jpg", List.of())
            );

            String key1 = "post:view:count:" + post1.getId();
            String key2 = "post:view:count:" + post2.getId();

            redisTemplate.opsForValue().set(key1, "5");
            redisTemplate.opsForValue().set(key2, "3");

            // when
            viewCountScheduler.flushViewCountToDB();

            // then
            assertThat(postCommandRepository.findById(post1.getId()).orElseThrow().getViewCount()).isEqualTo(5L);
            assertThat(postCommandRepository.findById(post2.getId()).orElseThrow().getViewCount()).isEqualTo(3L);

            // Redis 키 삭제 확인
            assertThat(redisTemplate.opsForValue().get(key1)).isNull();
            assertThat(redisTemplate.opsForValue().get(key2)).isNull();
        }
    }
}
