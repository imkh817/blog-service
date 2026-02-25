package study.blog.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.entity.Comment;
import study.blog.comment.enums.CommentStatus;
import study.blog.comment.exception.CommentNotFoundException;
import study.blog.comment.exception.InValidCommentContentException;
import study.blog.comment.exception.TooManyContentLengthException;
import study.blog.comment.repository.CommentQueryRepository;
import study.blog.comment.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 유스케이스 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentQueryRepository commentQueryRepository;

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("유효한 내용으로 댓글을 생성한다")
        void createComment_success() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            CreateCommentRequest request = new CreateCommentRequest("테스트 댓글 내용입니다.", null);

            Comment savedComment = Comment.createComment(postId, memberId, request.content(), request.parentId());
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(postId, memberId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.authorId()).isEqualTo(memberId);
            assertThat(response.content()).isEqualTo("테스트 댓글 내용입니다.");
            assertThat(response.parentId()).isNull();
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);

            then(commentRepository).should(times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("parentId를 포함한 대댓글을 생성한다")
        void createComment_withParentId_success() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            Long parentId = 10L;
            CreateCommentRequest request = new CreateCommentRequest("대댓글 내용입니다.", parentId);

            Comment savedComment = Comment.createComment(postId, memberId, request.content(), request.parentId());
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(postId, memberId, request);

            // then
            assertThat(response.parentId()).isEqualTo(parentId);
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
        }

        @Test
        @DisplayName("댓글 내용이 비어있으면 InValidCommentContentException이 발생한다")
        void createComment_emptyContent_throwsException() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            CreateCommentRequest request = new CreateCommentRequest("", null);

            // when, then
            assertThatThrownBy(
                    () -> commentService.createComment(postId, memberId, request)
            ).isInstanceOf(InValidCommentContentException.class);
        }

        @Test
        @DisplayName("댓글 내용이 공백이면 InValidCommentContentException이 발생한다")
        void createComment_blankContent_throwsException() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            CreateCommentRequest request = new CreateCommentRequest("   ", null);

            // when, then
            assertThatThrownBy(
                    () -> commentService.createComment(postId, memberId, request)
            ).isInstanceOf(InValidCommentContentException.class);
        }

        @Test
        @DisplayName("댓글 내용이 1000자를 초과하면 TooManyContentLengthException이 발생한다")
        void createComment_contentTooLong_throwsException() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            String longContent = "a".repeat(1001);
            CreateCommentRequest request = new CreateCommentRequest(longContent, null);

            // when, then
            assertThatThrownBy(
                    () -> commentService.createComment(postId, memberId, request)
            ).isInstanceOf(TooManyContentLengthException.class);
        }

        @Test
        @DisplayName("댓글 내용이 정확히 1000자이면 정상적으로 생성된다")
        void createComment_content1000Chars_success() {
            // given
            Long postId = 1L;
            Long memberId = 2L;
            String maxContent = "a".repeat(1000);
            CreateCommentRequest request = new CreateCommentRequest(maxContent, null);

            Comment savedComment = Comment.createComment(postId, memberId, request.content(), request.parentId());
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(postId, memberId, request);

            // then
            assertThat(response.content()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("단일 댓글 조회")
    class FindComment {

        @Test
        @DisplayName("postId와 commentId로 댓글을 조회한다")
        void findComment_success() {
            // given
            Long postId = 1L;
            Long commentId = 5L;
            Comment comment = Comment.createComment(postId, 2L, "댓글 내용", null);
            given(commentRepository.findByPostIdAndId(postId, commentId)).willReturn(Optional.of(comment));

            // when
            CommentResponse response = commentService.findComment(postId, commentId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.content()).isEqualTo("댓글 내용");
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 조회 시 CommentNotFoundException이 발생한다")
        void findComment_notFound_throwsException() {
            // given
            Long postId = 1L;
            Long commentId = 999L;
            given(commentRepository.findByPostIdAndId(postId, commentId)).willReturn(Optional.empty());

            // when, then
            assertThatThrownBy(
                    () -> commentService.findComment(postId, commentId)
            ).isInstanceOf(CommentNotFoundException.class)
             .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("댓글 목록 페이지 조회")
    class FindAllCommentsWithPaging {

        @Test
        @DisplayName("게시글의 댓글 목록을 페이지 단위로 조회한다")
        void findAllCommentWithPaging_success() {
            // given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            List<CommentViewResponse> comments = List.of(
                    new CommentViewResponse(null, postId, 1L, null, "첫 번째 댓글", null, CommentStatus.ACTIVE, null),
                    new CommentViewResponse(null, postId, 2L, null, "두 번째 댓글", null, CommentStatus.ACTIVE, null),
                    new CommentViewResponse(null, postId, 3L, null, "세 번째 댓글", null, CommentStatus.ACTIVE, null)
            );
            given(commentQueryRepository.findAllCommentsWithPaging(postId, pageable)).willReturn(comments);

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(postId, pageable);

            // then
            assertThat(responses).hasSize(3);
            assertThat(responses).extracting(CommentViewResponse::content)
                    .containsExactly("첫 번째 댓글", "두 번째 댓글", "세 번째 댓글");
            assertThat(responses).extracting(CommentViewResponse::postId)
                    .containsOnly(postId);
        }

        @Test
        @DisplayName("댓글이 없으면 빈 목록을 반환한다")
        void findAllCommentWithPaging_emptyList() {
            // given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            given(commentQueryRepository.findAllCommentsWithPaging(postId, pageable)).willReturn(List.of());

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(postId, pageable);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("부모 댓글과 대댓글이 함께 조회된다")
        void findAllCommentWithPaging_includesReplies() {
            // given
            Long postId = 1L;
            Long parentCommentId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            List<CommentViewResponse> comments = List.of(
                    new CommentViewResponse(null, postId, 1L, null, "부모 댓글", null, CommentStatus.ACTIVE, null),
                    new CommentViewResponse(null, postId, 2L, null, "대댓글 내용", parentCommentId, CommentStatus.ACTIVE, null)
            );
            given(commentQueryRepository.findAllCommentsWithPaging(postId, pageable)).willReturn(comments);

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(postId, pageable);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).parentId()).isNull();
            assertThat(responses.get(1).parentId()).isEqualTo(parentCommentId);
        }

        @Test
        @DisplayName("페이지 번호와 크기에 따라 댓글을 조회한다")
        void findAllCommentWithPaging_secondPage() {
            // given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(1, 2);

            List<CommentViewResponse> comments = List.of(
                    new CommentViewResponse(null, postId, 3L, null, "세 번째 댓글", null, CommentStatus.ACTIVE, null),
                    new CommentViewResponse(null, postId, 4L, null, "네 번째 댓글", null, CommentStatus.ACTIVE, null)
            );
            given(commentQueryRepository.findAllCommentsWithPaging(postId, pageable)).willReturn(comments);

            // when
            List<CommentViewResponse> responses = commentService.findAllCommentWithPaging(postId, pageable);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(CommentViewResponse::authorId)
                    .containsExactly(3L, 4L);
        }
    }
}
