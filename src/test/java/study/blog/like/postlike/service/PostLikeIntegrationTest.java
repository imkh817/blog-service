package study.blog.like.postlike.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import study.blog.global.IntegrationTestSupport;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.exception.DuplicatePostLikeException;
import study.blog.like.postlike.repository.PostLikeRepository;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class PostLikeIntegrationTest extends IntegrationTestSupport {

    @Autowired private PostLikeService postLikeService;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RedisTemplate redisTemplate;
    private final String KEY_PREFIX = "post:like:count:";
    @Test
    @DisplayName("100개의 동시 요청이 들어와도 게시글 좋아요가 정확히 처리되는지 검증하는 통합 테스트")
    void 게시글_좋아요_통합_테스트() throws InterruptedException {
        // given
        Long postId = 1L;
        List<Long> memberIds = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Member member = memberRepository.save(
                    Member.createMember("test" + i + "@naver.com", "password" + i, "nickname" + i)
            );
            memberIds.add(member.getId());
        }

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    PostLikeResponse postLikeResponse = postLikeService.likePost(memberId, postId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // then
        Long count = postLikeRepository.countByPostId(postId);

        assertThat(count).isEqualTo(100L);
    }

    @Test
    @DisplayName("DB 저장에 실패하면 Redis 카운트도 올라가지 않는다")
    void DB_저장에_실패하면_Redis_카운트도_올라가지_않는다(){
        Long postId = 1L;
        Long memberId = 100L;
        String key = KEY_PREFIX + postId;

        redisTemplate.opsForValue().set(key, "0");
        postLikeService.likePost(memberId, postId);

        // when: DB 저장 성공 후 이벤트가 발행되므로 DB 저장이 실패하면 Redis도 올라가지 않는다
        assertThatThrownBy(() -> postLikeService.likePost(memberId, postId))
                .isInstanceOf(DuplicatePostLikeException.class);

        // then: DB에는 1개의 좋아요만 저장됨
        assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(1L);

        // then: 두 번째 좋아요 시도는 DB 저장 실패 → 이벤트 미발행 → Redis 카운트 유지(1)
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(1L);
    }
}