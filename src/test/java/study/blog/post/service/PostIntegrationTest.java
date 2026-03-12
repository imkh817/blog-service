package study.blog.post.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import study.blog.global.IntegrationTestSupport;
import study.blog.postlike.application.PostLikeCommandService;
import study.blog.post.application.PostQueryService;
import study.blog.post.presentation.requset.PostSearchCondition;
import study.blog.post.presentation.response.PostSummaryResponse;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.event.PostViewedEvent;
import study.blog.post.domain.policy.ViewCountDeDuplicationPolicy;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;
import study.blog.post.infrastructure.redis.ViewCountRedisKeyGenerator;
import study.blog.post.application.ViewCountService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static study.blog.post.domain.PostStatus.*;

@Sql(
        statements = "DELETE FROM post_like",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class PostIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PostQueryService queryService;

    @Autowired
    private PostLikeCommandService postLikeCommandService;

    @Autowired
    private PostCommandRepository postCommandRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ViewCountService viewCountService;

    @Autowired
    private ViewCountDeDuplicationPolicy deDuplicationPolicy;

    @BeforeEach
    void setUp() {
        Post post1 = postCommandRepository.save(Post.createPost(
                1L, "Spring 입문 가이드", "Spring Boot를 활용한 웹 개발 입문",
                PUBLISHED, List.of("Java", "Backend"),
                "https://test-thumbnail.jpg", List.of()
        ));
        postLikeCommandService.likePost(post1.getId(), 1L);
        postLikeCommandService.likePost(post1.getId(), 2L);

        postCommandRepository.save(Post.createPost(
                1L, "QueryDSL 동적 쿼리 작성법", "복잡한 검색 조건을 QueryDSL로 해결하는 방법",
                PUBLISHED, List.of("Java", "Database"),
                "https://test-thumbnail.jpg", List.of()
        ));

        postCommandRepository.save(Post.createPost(
                1L, "Spring Security 인증 구현", "JWT 기반 인증 흐름을 구현한다",
                DRAFT, List.of("Java", "Security"),
                "https://test-thumbnail.jpg", List.of()
        ));

        postCommandRepository.save(Post.createPost(
                2L, "DDD 전술적 설계 패턴", "Aggregate와 Repository 패턴을 실무에 적용한다",
                PUBLISHED, List.of("Architecture", "Backend"),
                "https://test-thumbnail.jpg", List.of()
        ));

        postCommandRepository.save(Post.createPost(
                2L, "캐시 전략 정리", "Redis를 활용한 캐시 전략과 TTL 설정 가이드",
                DRAFT, List.of("Infrastructure", "Backend"),
                "https://test-thumbnail.jpg", List.of()
        ));

        postCommandRepository.save(Post.createPost(
                1L, "테스트 코드 작성 전략", "단위 테스트와 통합 테스트의 차이와 작성 전략",
                PUBLISHED, List.of("Java", "Testing"),
                "https://test-thumbnail.jpg", List.of()
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("키워드 검색")
    class KeywordSearch {

        @Test
        @DisplayName("제목에 키워드가 포함된 게시글을 조회한다")
        void searchByTitleKeyword() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "Spring", null, null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder("Spring 입문 가이드", "Spring Security 인증 구현");
        }

        @Test
        @DisplayName("본문에 키워드가 포함된 게시글을 조회한다")
        void searchByContentKeyword() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "Redis", null, null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly("캐시 전략 정리");
        }

        @Test
        @DisplayName("제목과 본문 모두에 키워드가 포함되어도 중복 없이 조회된다")
        void searchByKeywordInBothTitleAndContent() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "테스트", null, null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly("테스트 코드 작성 전략");
        }

        @Test
        @DisplayName("키워드에 매칭되는 게시글이 없으면 빈 리스트를 반환한다")
        void searchByKeywordNoMatch() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "존재하지않는키워드", null, null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("태그 검색")
    class TagSearch {

        @Test
        @DisplayName("단일 태그로 게시글을 조회한다")
        void searchBySingleTag() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Security"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly("Spring Security 인증 구현");
        }

        @Test
        @DisplayName("여러 태그 중 하나라도 일치하면 게시글을 조회한다")
        void searchByMultipleTags() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Database", "Testing"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder("QueryDSL 동적 쿼리 작성법", "테스트 코드 작성 전략");
        }

        @Test
        @DisplayName("공통 태그로 검색하면 해당 태그를 가진 모든 게시글을 조회한다")
        void searchByCommonTag() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Backend"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(3)
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder(
                            "Spring 입문 가이드",
                            "DDD 전술적 설계 패턴",
                            "캐시 전략 정리"
                    );
        }

        @Test
        @DisplayName("존재하지 않는 태그로 검색하면 빈 리스트를 반환한다")
        void searchByNonExistentTag() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("NonExistentTag"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 상태 검색")
    class StatusSearch {

        @Test
        @DisplayName("PUBLISHED 상태의 게시글만 조회한다")
        void searchByPublishedStatus() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, List.of(PUBLISHED), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(4)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(PUBLISHED)
                    )
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder(
                            "Spring 입문 가이드",
                            "QueryDSL 동적 쿼리 작성법",
                            "DDD 전술적 설계 패턴",
                            "테스트 코드 작성 전략"
                    );
        }

        @Test
        @DisplayName("DRAFT 상태의 게시글만 조회한다")
        void searchByDraftStatus() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, List.of(DRAFT), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(DRAFT)
                    )
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder(
                            "Spring Security 인증 구현",
                            "캐시 전략 정리"
                    );
        }

        @Test
        @DisplayName("여러 상태를 동시에 필터링하여 조회한다")
        void searchByMultipleStatuses() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, List.of(DRAFT, PUBLISHED), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(6);
        }

        @Test
        @DisplayName("HIDDEN 상태의 게시글이 없으면 빈 리스트를 반환한다")
        void searchByHiddenStatusNoMatch() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, List.of(HIDDEN), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("작성일 범위 검색")
    class DateRangeSearch {

        @Test
        @DisplayName("시작일 이후에 작성된 게시글을 조회한다")
        void searchFromDate() {
            // given
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, yesterday, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(6);
        }

        @Test
        @DisplayName("종료일 이전에 작성된 게시글을 조회한다")
        void searchToDate() {
            // given
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, tomorrow
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(6);
        }

        @Test
        @DisplayName("시작일과 종료일 사이에 작성된 게시글을 조회한다")
        void searchByDateRange() {
            // given
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, yesterday, tomorrow
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(6);
        }

        @Test
        @DisplayName("미래 시작일로 검색하면 빈 리스트를 반환한다")
        void searchFromFutureDate() {
            // given
            LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, futureDate, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("과거 종료일로 검색하면 빈 리스트를 반환한다")
        void searchToPastDate() {
            // given
            LocalDateTime pastDate = LocalDateTime.now().minusDays(30);
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, pastDate
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("복합 조건 검색")
    class CombinedSearch {

        @Test
        @DisplayName("키워드와 상태를 동시에 필터링하여 조회한다")
        void searchByKeywordAndStatus() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "Spring", null, List.of(PUBLISHED), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title, PostSummaryResponse::postStatus)
                    .containsExactly(tuple("Spring 입문 가이드", PUBLISHED));
        }

        @Test
        @DisplayName("키워드와 태그를 동시에 필터링하여 조회한다")
        void searchByKeywordAndTag() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "Spring", List.of("Backend"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly("Spring 입문 가이드");
        }

        @Test
        @DisplayName("태그와 상태를 동시에 필터링하여 조회한다")
        void searchByTagAndStatus() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Java"), List.of(PUBLISHED), null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(3)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(PUBLISHED)
                    )
                    .extracting(PostSummaryResponse::title)
                    .containsExactlyInAnyOrder(
                            "Spring 입문 가이드",
                            "QueryDSL 동적 쿼리 작성법",
                            "테스트 코드 작성 전략"
                    );
        }

        @Test
        @DisplayName("키워드, 태그, 상태를 모두 조합하여 조회한다")
        void searchByKeywordAndTagAndStatus() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "입문",
                    List.of("Java", "Backend"),
                    List.of(PUBLISHED),
                    null,
                    null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title, PostSummaryResponse::postStatus)
                    .containsExactly(tuple("Spring 입문 가이드", PUBLISHED));
        }

        @Test
        @DisplayName("키워드, 태그, 상태, 날짜 범위를 모두 조합하여 조회한다")
        void searchByAllConditions() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "입문",
                    List.of("Java", "Backend"),
                    List.of(PUBLISHED),
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1)
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostSummaryResponse::title, PostSummaryResponse::postStatus)
                    .containsExactly(tuple("Spring 입문 가이드", PUBLISHED));
        }

        @Test
        @DisplayName("모든 조건을 조합했을 때 일치하는 게시글이 없으면 빈 리스트를 반환한다")
        void searchByAllConditionsNoMatch() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    "Spring",
                    List.of("Architecture"),
                    List.of(PUBLISHED),
                    null,
                    null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @Test
        @DisplayName("태그 조건으로 필터링된 결과를 페이지 크기만큼 조회한다")
        void searchWithPageSize() {
            // given - Backend 태그: Spring 입문 가이드, DDD 전술적 설계 패턴, 캐시 전략 정리 (3건)
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Backend"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 2));

            // then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("두 번째 페이지를 조회하면 나머지 게시글이 반환된다")
        void searchSecondPage() {
            // given - Backend 태그: 3건
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Backend"), null, null, null
            );

            // when
            Page<PostSummaryResponse> firstPage = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 2));
            Page<PostSummaryResponse> secondPage = queryService.searchPostByCondition(
                    condition, PageRequest.of(1, 2));

            // then
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(1);

            List<Long> firstPageIds = firstPage.stream().map(PostSummaryResponse::postId).toList();
            List<Long> secondPageIds = secondPage.stream().map(PostSummaryResponse::postId).toList();
            assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
        }

        @Test
        @DisplayName("데이터 범위를 초과한 페이지를 조회하면 빈 리스트를 반환한다")
        void searchBeyondLastPage() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Java"), null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(100, 10));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("조건 없이 검색")
    class NoConditionSearch {

        @Test
        @DisplayName("모든 조건이 null이면 전체 게시글을 조회한다")
        void searchWithNoCondition() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(6);
        }
    }

    @Nested
    @DisplayName("정렬 검색")
    class OrderBySearch {

        @Test
        @DisplayName("작성일 내림차순으로 정렬하여 조회한다")
        void searchOrderByCreatedAtDesc() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );
            PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.getContent().get(0).title()).isEqualTo("테스트 코드 작성 전략");
            assertThat(results.getContent().get(results.getContent().size() - 1).title()).isEqualTo("Spring 입문 가이드");
        }

        @Test
        @DisplayName("작성일 오름차순으로 정렬하여 조회한다")
        void searchOrderByCreatedAtAsc() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );
            PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "createdAt"));

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.getContent().get(0).title()).isEqualTo("Spring 입문 가이드");
            assertThat(results.getContent().get(results.getContent().size() - 1).title()).isEqualTo("테스트 코드 작성 전략");
        }

        @Test
        @DisplayName("조회수 내림차순으로 정렬하여 조회한다")
        void searchOrderByViewCountDesc() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );
            PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "viewCount"));

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
        }

        @Test
        @DisplayName("지원하지 않는 정렬 기준이면 기본 정렬(작성일 내림차순)으로 조회한다")
        void searchOrderByUnsupportedProperty() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );
            PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "unknownField"));

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.getContent().get(0).title()).isEqualTo("테스트 코드 작성 전략");
            assertThat(results.getContent().get(results.getContent().size() - 1).title()).isEqualTo("Spring 입문 가이드");
        }

        @Test
        @DisplayName("정렬 조건과 검색 조건을 함께 사용하여 조회한다")
        void searchWithConditionAndOrderBy() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, List.of("Java"), List.of(PUBLISHED), null, null
            );
            PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "createdAt"));

            // when
            Page<PostSummaryResponse> results = queryService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(3)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly(
                            "Spring 입문 가이드",
                            "QueryDSL 동적 쿼리 작성법",
                            "테스트 코드 작성 전략"
                    );
        }

        @Test
        @DisplayName("정렬 조건과 페이지네이션을 함께 사용하여 조회한다")
        void searchWithOrderByAndPagination() {
            // given
            PostSearchCondition condition = new PostSearchCondition(
                    null, null, null, null, null
            );
            PageRequest firstPage = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "createdAt"));
            PageRequest secondPage = PageRequest.of(1, 3, Sort.by(Sort.Direction.ASC, "createdAt"));

            // when
            Page<PostSummaryResponse> firstResults = queryService.searchPostByCondition(condition, firstPage);
            Page<PostSummaryResponse> secondResults = queryService.searchPostByCondition(condition, secondPage);

            // then
            assertThat(firstResults).hasSize(3)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly(
                            "Spring 입문 가이드",
                            "QueryDSL 동적 쿼리 작성법",
                            "Spring Security 인증 구현"
                    );
            assertThat(secondResults).hasSize(3)
                    .extracting(PostSummaryResponse::title)
                    .containsExactly(
                            "DDD 전술적 설계 패턴",
                            "캐시 전략 정리",
                            "테스트 코드 작성 전략"
                    );
        }
    }

    @Nested
    @DisplayName("조회수 Redis TPS 검증")
    class ViewCountTest {

        @Test
        @DisplayName("조회수 증가 Redis TPS 확인")
        void Redis_TPS_확인() throws InterruptedException {
            // given
            Post post = postCommandRepository.findAll().getFirst();
            Long postId = post.getId();

            int totalRequests = 1000;
            int threadPoolSize = 50;
            CountDownLatch latch = new CountDownLatch(totalRequests);

            // when
            long startTime = System.currentTimeMillis();

            try (ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize)) {
                for (int i = 0; i < totalRequests; i++) {
                    executor.submit(() -> {
                        try {
                            viewCountService.increaseViewCount(postId);
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                boolean completed = latch.await(30, TimeUnit.SECONDS);
                assertThat(completed).as("30초 내에 모든 요청이 완료되어야 한다").isTrue();
            }

            long elapsedMs = System.currentTimeMillis() - startTime;

            // then
            String key = ViewCountRedisKeyGenerator.generateViewCountKey(postId);
            String rawCount = stringRedisTemplate.opsForValue().get(key);
            assertThat(rawCount).as("Redis 조회수 키가 존재해야 한다").isNotNull();

            Long actualCount = Long.parseLong(rawCount);
            double tps = totalRequests / (elapsedMs / 1000.0);

            System.out.printf("[TPS 측정] 총 %,d건 / %.3f초 → TPS %.0f%n",
                    totalRequests, elapsedMs / 1000.0, tps);

            assertThat(actualCount).isEqualTo(totalRequests);
        }

        @Test
        @DisplayName("중복 조회 방지 정책 적용 시 고유 사용자 수만큼만 조회수가 증가한다")
        void 중복_제거_후_고유_조회수_확인() throws Exception {
            // given
            Long postId = postCommandRepository.findAll().getFirst().getId();

            int uniqueUsers = 1_000;
            int requestsPerUser = 100;
            int total = uniqueUsers * requestsPerUser;
            int threads = 8;

            CountDownLatch done = new CountDownLatch(total);
            long startNs = System.nanoTime();

            // when
            try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
                for (int i = 0; i < uniqueUsers; i++) {
                    long userId = i;
                    for (int j = 0; j < requestsPerUser; j++) {
                        executor.submit(() -> {
                            try {
                                PostViewedEvent event = new PostViewedEvent(userId, postId, null);
                                if (deDuplicationPolicy.allow(event)) {
                                    viewCountService.increaseViewCount(postId);
                                }
                            } finally {
                                done.countDown();
                            }
                        });
                    }
                }

                boolean completed = done.await(60, TimeUnit.SECONDS);
                assertThat(completed).as("60초 내에 모든 요청이 완료되어야 한다").isTrue();
            }

            long elapsedNs = System.nanoTime() - startNs;
            double sec = elapsedNs / 1_000_000_000.0;
            double tps = total / sec;

            System.out.printf("[실험] 총 %,d건 (고유 사용자 %,d명 × %d회) / 총 걸린 시간: %.2fs → 초당 처리량: %.0f ops/s%n",
                    total, uniqueUsers, requestsPerUser, sec, tps);

            // then
            String key = ViewCountRedisKeyGenerator.generateViewCountKey(postId);
            String rawCount = stringRedisTemplate.opsForValue().get(key);
            assertThat(rawCount).as("Redis 조회수 키가 존재해야 한다").isNotNull();

            Long actualCount = Long.parseLong(rawCount);
            System.out.printf("[결과] 기대 조회수: %,d / 실제 조회수: %,d%n", (long) uniqueUsers, actualCount);
            assertThat(actualCount).isEqualTo(uniqueUsers);
        }
    }
}
