package study.blog.like.postlike.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.blog.like.postlike.exception.DuplicatePostLikeException;
import study.blog.like.postlike.repository.PostLikeRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @InjectMocks
    private PostLikeService postLikeService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("이미 좋아요를 누른 게시글에는 다시 좋아요를 누를 수없다")
    void 이미_좋아요를_누른_게시글에는_다시_좋아요를_누를_수_없다() {
        // given
        Long postId = 1L;
        Long memberId = 100L;

        // when
        when(postLikeRepository.existsByMemberIdAndPostId(memberId, postId)).thenReturn(true);

        // then
        Assertions.assertThatThrownBy(() -> postLikeService.likePost(memberId, postId))
                .isInstanceOf(DuplicatePostLikeException.class)
                .hasMessage("이미 좋아요를 누른 게시글입니다.");
    }
}