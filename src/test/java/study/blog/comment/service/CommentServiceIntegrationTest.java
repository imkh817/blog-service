package study.blog.comment.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.entity.Comment;
import study.blog.comment.enums.CommentStatus;
import study.blog.comment.exception.CommentNotFoundException;
import study.blog.comment.repository.CommentRepository;
import study.blog.global.IntegrationTestSupport;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommentService 통합 테스트")
class CommentServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private static final Long POST_ID = 1L;
    private static final Long OTHER_POST_ID = 2L;
    private Long memberId;

    @BeforeEach
    void setUp() {
        Member member = Member.createMember("test@naver.com", "12345678", "테스트유저10");
        Member savedMember = memberRepository.save(member);
        memberId = savedMember.getId();
        commentRepository.save(Comment.createComment(POST_ID, memberId, "첫 번째 댓글", null));
        commentRepository.save(Comment.createComment(POST_ID, memberId, "두 번째 댓글", null));
        commentRepository.save(Comment.createComment(POST_ID, memberId, "세 번째 댓글", null));
        commentRepository.save(Comment.createComment(OTHER_POST_ID, memberId, "다른 게시글 댓글", null));

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("생성된 댓글이 DB에 영속화된다")
        void createComment_persistsToDatabase() {
            // given
            CreateCommentRequest request = new CreateCommentRequest("새 댓글 내용", null);

            // when
            CommentResponse response = commentService.createComment(POST_ID, memberId, request);
            entityManager.flush();
            entityManager.clear();

            // then
            Comment saved = commentRepository.findById(response.commentId()).orElseThrow();
            assertThat(saved.getContent()).isEqualTo("새 댓글 내용");
            assertThat(saved.getPostId()).isEqualTo(POST_ID);
            assertThat(saved.getAuthorId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("생성된 댓글의 초기 상태는 ACTIVE이다")
        void createComment_initialStatusIsActive() {
            // given
            CreateCommentRequest request = new CreateCommentRequest("댓글 내용", null);

            // when
            CommentResponse response = commentService.createComment(POST_ID, memberId, request);

            // then
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
        }

        @Test
        @DisplayName("생성된 댓글의 createdAt이 자동으로 설정된다")
        void createComment_createdAtIsAutoPopulated() {
            // given
            CreateCommentRequest request = new CreateCommentRequest("댓글 내용", null);

            // when
            CommentResponse response = commentService.createComment(POST_ID, memberId, request);
            entityManager.flush();
            entityManager.clear();

            // then
            Comment saved = commentRepository.findById(response.commentId()).orElseThrow();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getEditedAt()).isNotNull();
        }

        @Test
        @DisplayName("대댓글이 parentId와 함께 DB에 저장된다")
        void createComment_nestedReply_persistsWithParentId() {
            // given
            Long parentCommentId = 10L;
            CreateCommentRequest request = new CreateCommentRequest("대댓글 내용", parentCommentId);

            // when
            CommentResponse response = commentService.createComment(POST_ID, memberId, request);
            entityManager.flush();
            entityManager.clear();

            // then
            Comment saved = commentRepository.findById(response.commentId()).orElseThrow();
            assertThat(saved.getParentId()).isEqualTo(parentCommentId);
            assertThat(response.parentId()).isEqualTo(parentCommentId);
        }

        @Test
        @DisplayName("댓글 내용이 비어있으면 저장되지 않는다")
        void createComment_emptyContent_doesNotPersist() {
            // given
            long countBefore = commentRepository.count();
            CreateCommentRequest request = new CreateCommentRequest("", null);

            // when, then
            assertThatThrownBy(
                    () -> commentService.createComment(POST_ID, memberId, request)
            ).isInstanceOf(RuntimeException.class);

            assertThat(commentRepository.count()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("단일 댓글 조회")
    class FindComment {

        @Test
        @DisplayName("postId와 commentId로 댓글을 정확히 조회한다")
        void findComment_success() {
            // given
            Comment saved = commentRepository.save(
                    Comment.createComment(POST_ID, memberId, "조회 대상 댓글", null));
            entityManager.flush();
            entityManager.clear();

            // when
            CommentResponse response = commentService.findComment(POST_ID, saved.getId());

            // then
            assertThat(response.commentId()).isEqualTo(saved.getId());
            assertThat(response.content()).isEqualTo("조회 대상 댓글");
            assertThat(response.postId()).isEqualTo(POST_ID);
            assertThat(response.authorId()).isEqualTo(memberId);
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
        }

        @Test
        @DisplayName("다른 게시글의 commentId로 조회하면 CommentNotFoundException이 발생한다")
        void findComment_wrongPostId_throwsException() {
            // given
            Comment saved = commentRepository.save(
                    Comment.createComment(OTHER_POST_ID, memberId, "다른 게시글 댓글", null));
            entityManager.flush();
            entityManager.clear();

            // when, then
            assertThatThrownBy(
                    () -> commentService.findComment(POST_ID, saved.getId())
            ).isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 commentId로 조회하면 CommentNotFoundException이 발생한다")
        void findComment_nonExistentId_throwsException() {
            // given
            Long nonExistentId = Long.MAX_VALUE;

            // when, then
            assertThatThrownBy(
                    () -> commentService.findComment(POST_ID, nonExistentId)
            ).isInstanceOf(CommentNotFoundException.class)
             .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("댓글 목록 페이지 조회")
    class FindAllCommentsWithPaging {

        @Test
        @DisplayName("해당 게시글의 댓글만 조회한다")
        void findAllCommentWithPaging_filtersByPostId() {
            // given - setUp: POST_ID 3개, OTHER_POST_ID 1개

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 100));

            System.out.println("responses = " + responses);

            // then
            assertThat(responses).hasSize(3);
            assertThat(responses).extracting(CommentViewResponse::postId)
                    .containsOnly(POST_ID);
            assertThat(responses).extracting(CommentViewResponse::authorId)
                    .contains(memberId);
        }

        @Test
        @DisplayName("댓글이 없는 게시글 조회 시 빈 목록이 반환된다")
        void findAllCommentWithPaging_noComments_returnsEmpty() {
            // given
            Long emptyPostId = 999L;

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    emptyPostId, PageRequest.of(0, 10));

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("페이지 크기만큼만 댓글을 조회한다")
        void findAllCommentWithPaging_respectsPageSize() {
            // given - setUp: POST_ID 3개, pageSize=2

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 2));

            // then
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("두 번째 페이지를 조회하면 중복 없이 나머지 댓글이 반환된다")
        void findAllCommentWithPaging_secondPage_returnsRemainingComments() {
            // given - setUp: POST_ID 3개, pageSize=2
            List<CommentViewResponse> firstPage = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 2));

            // when
            List<CommentViewResponse> secondPage = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(1, 2));

            // then
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(1);

            List<Long> firstPageIds = firstPage.stream().map(CommentViewResponse::commentId).toList();
            List<Long> secondPageIds = secondPage.stream().map(CommentViewResponse::commentId).toList();
            assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
        }

        @Test
        @DisplayName("범위를 초과한 페이지 조회 시 빈 목록이 반환된다")
        void findAllCommentWithPaging_beyondLastPage_returnsEmpty() {
            // given - setUp: POST_ID 3개

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(100, 10));

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("createdAt 오름차순으로 정렬하여 조회한다")
        void findAllCommentWithPaging_sortByCreatedAtAsc() throws InterruptedException {
            // given - setUp 데이터 제거 후 시간 차이를 두고 재생성
            commentRepository.deleteAll();

            Comment older = commentRepository.save(Comment.createComment(POST_ID, memberId, "오래된 댓글", null));
            entityManager.flush();
            Thread.sleep(10);
            Comment newer = commentRepository.save(Comment.createComment(POST_ID, memberId, "최신 댓글", null));
            entityManager.flush();
            entityManager.clear();

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")));

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).commentId()).isEqualTo(older.getId());
            assertThat(responses.get(1).commentId()).isEqualTo(newer.getId());
        }

        @Test
        @DisplayName("createdAt 내림차순으로 정렬하여 최신 댓글이 먼저 조회된다")
        void findAllCommentWithPaging_sortByCreatedAtDesc() throws InterruptedException {
            // given - setUp 데이터 제거 후 시간 차이를 두고 재생성
            commentRepository.deleteAll();

            Comment older = commentRepository.save(Comment.createComment(POST_ID, memberId, "오래된 댓글", null));
            entityManager.flush();
            Thread.sleep(10);
            Comment newer = commentRepository.save(Comment.createComment(POST_ID, memberId, "최신 댓글", null));
            entityManager.flush();
            entityManager.clear();

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).commentId()).isEqualTo(newer.getId());
            assertThat(responses.get(1).commentId()).isEqualTo(older.getId());
        }

        @Test
        @DisplayName("지원하지 않는 정렬 기준은 기본 정렬(createdAt 내림차순)로 처리된다")
        void findAllCommentWithPaging_unsupportedSortProperty_fallsBackToDefault() throws InterruptedException {
            // given - setUp 데이터 제거 후 시간 차이를 두고 재생성
            commentRepository.deleteAll();

            Comment older = commentRepository.save(Comment.createComment(POST_ID, memberId, "오래된 댓글", null));
            entityManager.flush();
            Thread.sleep(10);
            Comment newer = commentRepository.save(Comment.createComment(POST_ID, memberId, "최신 댓글", null));
            entityManager.flush();
            entityManager.clear();

            // when - 알 수 없는 정렬 속성 사용 시 defaultOrder() (createdAt DESC) 적용
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "unknownField")));

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).commentId()).isEqualTo(newer.getId());
            assertThat(responses.get(1).commentId()).isEqualTo(older.getId());
        }

        @Test
        @DisplayName("대댓글과 부모 댓글이 함께 조회된다")
        void findAllCommentWithPaging_includesRepliesWithParentId() {
            // given
            Long parentCommentId = 10L;
            commentRepository.save(Comment.createComment(POST_ID, memberId, "대댓글 내용", parentCommentId));
            entityManager.flush();
            entityManager.clear();

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(
                    POST_ID, PageRequest.of(0, 100));

            // then - setUp 3개 + 대댓글 1개
            assertThat(responses).hasSize(4);
            assertThat(responses).anySatisfy(r -> assertThat(r.parentId()).isEqualTo(parentCommentId));
            assertThat(responses).filteredOn(r -> r.parentId() == null).hasSize(3);
        }
    }
}
