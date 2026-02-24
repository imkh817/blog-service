package study.blog.like.postlike.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.global.web.resolver.LoginMemberId;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.service.PostLikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse> likePost(@PathVariable Long postId, @LoginMember Long memberId) {
        PostLikeResponse response = postLikeService.likePost(postId, memberId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{postId}/likes")
    public ApiResponse<PostLikeResponse> unlikePost(@PathVariable Long postId, @LoginMember Long memberId) {
        PostLikeResponse response = postLikeService.unlikePost(postId, memberId);
        return ApiResponse.success(response);
    }

}
