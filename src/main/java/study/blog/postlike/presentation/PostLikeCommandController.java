package study.blog.postlike.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.postlike.application.PostLikeCommandService;
import study.blog.postlike.presentation.response.PostLikeResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostLikeCommandController {

    private final PostLikeCommandService postLikeCommandService;

    /**
     * 게시글에 좋아요를 추가한다.
     *
     * 로그인한 사용자가 특정 게시글에 좋아요를 누른다.
     *
     * @param postId   좋아요를 누를 게시글 ID
     * @param memberId 로그인 사용자 ID
     * @return 좋아요 처리 결과
     */
    @PostMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse> likePost(@PathVariable Long postId, @LoginMember Long memberId) {
        PostLikeResponse response = postLikeCommandService.likePost(postId, memberId);
        return ApiResponse.success(response);
    }

    /**
     * 게시글의 좋아요를 취소한다.
     *
     * 로그인한 사용자가 특정 게시글에 눌렀던 좋아요를 취소한다.
     *
     * @param postId   좋아요를 취소할 게시글 ID
     * @param memberId 로그인 사용자 ID
     * @return 좋아요 취소 처리 결과
     */
    @DeleteMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse> unlikePost(@PathVariable Long postId, @LoginMember Long memberId) {
        PostLikeResponse response = postLikeCommandService.unlikePost(postId, memberId);
        return ApiResponse.success(response);
    }

}
