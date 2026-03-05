package study.blog.like.postlike.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.event.LikeCountTracker;
import study.blog.like.postlike.exception.DuplicatePostLikeException;
import study.blog.like.postlike.exception.LikeNotFoundException;
import study.blog.like.postlike.repository.command.PostLikeCommandRepository;
import study.blog.like.postlike.repository.query.PostLikeQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostLikeCommandService 단위 테스트")
class PostLikeCommandServiceTest {

    @InjectMocks
    private PostLikeCommandService postLikeCommandService;

    @Mock
    private PostLikeQueryRepository postLikeQueryRepository;

    @Mock
    private PostLikeCommandRepository postLikeCommandRepository;

    @Mock
    private LikeCountTracker likeCountTracker;

    @Nested
    @DisplayName("게시글 좋아요")
    class LikePost {

        @Test
        @DisplayName("성공 - likeCount와 liked=true를 반환한다")
        void likePost_success() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.save(any())).willReturn(null);
            given(postLikeQueryRepository.countByPostId(postId)).willReturn(5L);

            // when
            PostLikeResponse response = postLikeCommandService.likePost(postId, memberId);

            // then
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.likeCount()).isEqualTo(5L);
            assertThat(response.liked()).isTrue();
        }

        @Test
        @DisplayName("성공 - 좋아요 후 LikeCountTracker가 호출된다")
        void likePost_trackerCalled() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.save(any())).willReturn(null);
            given(postLikeQueryRepository.countByPostId(postId)).willReturn(1L);

            // when
            postLikeCommandService.likePost(postId, memberId);

            // then
            then(likeCountTracker).should().track(postId, 1);
        }

        @Test
        @DisplayName("이미 좋아요를 누른 게시글에는 DuplicatePostLikeException이 발생한다")
        void likePost_duplicate_throwsException() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.save(any()))
                    .willThrow(DataIntegrityViolationException.class);

            // when, then
            assertThatThrownBy(() -> postLikeCommandService.likePost(postId, memberId))
                    .isInstanceOf(DuplicatePostLikeException.class)
                    .hasMessageContaining("이미 좋아요를 누른 게시글입니다.");
        }

        @Test
        @DisplayName("중복 좋아요 실패 시 LikeCountTracker는 호출되지 않는다")
        void likePost_duplicate_trackerNotCalled() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.save(any()))
                    .willThrow(DataIntegrityViolationException.class);

            // when
            assertThatThrownBy(() -> postLikeCommandService.likePost(postId, memberId))
                    .isInstanceOf(DuplicatePostLikeException.class);

            // then
            then(likeCountTracker).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("게시글 좋아요 취소")
    class UnlikePost {

        @Test
        @DisplayName("성공 - likeCount와 liked=false를 반환한다")
        void unlikePost_success() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.deleteByMemberIdAndPostId(memberId, postId)).willReturn(1);
            given(postLikeQueryRepository.countByPostId(postId)).willReturn(4L);

            // when
            PostLikeResponse response = postLikeCommandService.unlikePost(postId, memberId);

            // then
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.likeCount()).isEqualTo(4L);
            assertThat(response.liked()).isFalse();
        }

        @Test
        @DisplayName("성공 - 좋아요 취소 후 LikeCountTracker가 -1로 호출된다")
        void unlikePost_trackerCalled() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.deleteByMemberIdAndPostId(memberId, postId)).willReturn(1);
            given(postLikeQueryRepository.countByPostId(postId)).willReturn(0L);

            // when
            postLikeCommandService.unlikePost(postId, memberId);

            // then
            then(likeCountTracker).should().track(postId, -1);
        }

        @Test
        @DisplayName("좋아요를 누르지 않은 게시글 취소 시 LikeNotFoundException이 발생한다")
        void unlikePost_notLiked_throwsException() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.deleteByMemberIdAndPostId(memberId, postId)).willReturn(0);

            // when, then
            assertThatThrownBy(() -> postLikeCommandService.unlikePost(postId, memberId))
                    .isInstanceOf(LikeNotFoundException.class)
                    .hasMessageContaining("좋아요를 누르지 않은 게시글입니다.");
        }

        @Test
        @DisplayName("좋아요 취소 실패 시 LikeCountTracker는 호출되지 않는다")
        void unlikePost_notLiked_trackerNotCalled() {
            // given
            Long postId = 1L;
            Long memberId = 100L;

            given(postLikeCommandRepository.deleteByMemberIdAndPostId(memberId, postId)).willReturn(0);

            // when
            assertThatThrownBy(() -> postLikeCommandService.unlikePost(postId, memberId))
                    .isInstanceOf(LikeNotFoundException.class);

            // then
            then(likeCountTracker).shouldHaveNoInteractions();
        }
    }
}
