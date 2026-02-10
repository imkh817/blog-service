package study.blog.post.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import study.blog.global.IntegrationTestSupport;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;
import study.blog.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static study.blog.post.enums.PostStatus.*;

class PostIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        postRepository.save(Post.createPost(
                1L, "Spring 입문 가이드", "Spring Boot를 활용한 웹 개발 입문",
                PUBLISHED, List.of("Java", "Backend")
        ));

        postRepository.save(Post.createPost(
                1L, "QueryDSL 동적 쿼리 작성법", "복잡한 검색 조건을 QueryDSL로 해결하는 방법",
                PUBLISHED, List.of("Java", "Database")
        ));

        postRepository.save(Post.createPost(
                1L, "Spring Security 인증 구현", "JWT 기반 인증 흐름을 구현한다",
                DRAFT, List.of("Java", "Security")
        ));

        postRepository.save(Post.createPost(
                2L, "DDD 전술적 설계 패턴", "Aggregate와 Repository 패턴을 실무에 적용한다",
                PUBLISHED, List.of("Architecture", "Backend")
        ));

        postRepository.save(Post.createPost(
                2L, "캐시 전략 정리", "Redis를 활용한 캐시 전략과 TTL 설정 가이드",
                DRAFT, List.of("Infrastructure", "Backend")
        ));

        postRepository.save(Post.createPost(
                1L, "테스트 코드 작성 전략", "단위 테스트와 통합 테스트의 차이와 작성 전략",
                PUBLISHED, List.of("Java", "Testing")
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(3)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(4)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(PUBLISHED)
                    )
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(2)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(DRAFT)
                    )
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title, PostResponse::postStatus)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(3)
                    .allSatisfy(response ->
                            assertThat(response.postStatus()).isEqualTo(PUBLISHED)
                    )
                    .extracting(PostResponse::title)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title, PostResponse::postStatus)
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
            List<PostResponse> results = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 100));

            // then
            assertThat(results).hasSize(1)
                    .extracting(PostResponse::title, PostResponse::postStatus)
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> firstPage = postService.searchPostByCondition(
                    condition, PageRequest.of(0, 2));
            List<PostResponse> secondPage = postService.searchPostByCondition(
                    condition, PageRequest.of(1, 2));

            // then
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(1);

            List<Long> firstPageIds = firstPage.stream().map(PostResponse::postId).toList();
            List<Long> secondPageIds = secondPage.stream().map(PostResponse::postId).toList();
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(
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
            List<PostResponse> results = postService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.get(0).title()).isEqualTo("테스트 코드 작성 전략");
            assertThat(results.get(results.size() - 1).title()).isEqualTo("Spring 입문 가이드");
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
            List<PostResponse> results = postService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.get(0).title()).isEqualTo("Spring 입문 가이드");
            assertThat(results.get(results.size() - 1).title()).isEqualTo("테스트 코드 작성 전략");
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
            List<PostResponse> results = postService.searchPostByCondition(condition, pageable);

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
            List<PostResponse> results = postService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(6);
            assertThat(results.get(0).title()).isEqualTo("테스트 코드 작성 전략");
            assertThat(results.get(results.size() - 1).title()).isEqualTo("Spring 입문 가이드");
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
            List<PostResponse> results = postService.searchPostByCondition(condition, pageable);

            // then
            assertThat(results).hasSize(3)
                    .extracting(PostResponse::title)
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
            List<PostResponse> firstResults = postService.searchPostByCondition(condition, firstPage);
            List<PostResponse> secondResults = postService.searchPostByCondition(condition, secondPage);

            // then
            assertThat(firstResults).hasSize(3)
                    .extracting(PostResponse::title)
                    .containsExactly(
                            "Spring 입문 가이드",
                            "QueryDSL 동적 쿼리 작성법",
                            "Spring Security 인증 구현"
                    );
            assertThat(secondResults).hasSize(3)
                    .extracting(PostResponse::title)
                    .containsExactly(
                            "DDD 전술적 설계 패턴",
                            "캐시 전략 정리",
                            "테스트 코드 작성 전략"
                    );
        }
    }
}
