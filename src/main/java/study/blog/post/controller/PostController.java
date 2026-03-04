package study.blog.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.post.dto.*;
import study.blog.post.service.PostService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatePostResponse> createPost(@LoginMember Long memberId,
                                                @Valid @RequestBody CreatePostDto createPostDto){
        CreatePostResponse post = postService.createPost(memberId, createPostDto);
        return ApiResponse.success(post);
    }

    @PutMapping
    public ApiResponse<ModifyPostResponse> modifyPost(@LoginMember Long memberId,
                                                @Valid @RequestBody UpdatePostDto updatePostDto){
        ModifyPostResponse response = postService.modifyPost(memberId, updatePostDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/publish")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToPublish(@PathVariable Long postId){
        PostStatusUpdateResponse response = postService.changeStatusToPublish(postId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/hide")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToHidden(@PathVariable Long postId){
        PostStatusUpdateResponse response = postService.changeStatusToHidden(postId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/delete")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToDelete(@PathVariable Long postId){
        PostStatusUpdateResponse response = postService.changeStatusToDelete(postId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> findPost(@PathVariable Long postId,
                                              @LoginMember(required = false) Long memberId,
                                              HttpServletRequest request){
        PostResponse post = postService.findPost(postId, memberId, request);
        return ApiResponse.success(post);
    }

    @GetMapping("/my")
    public ApiResponse<List<PostResponse>> findPostsByAuthorId(@LoginMember Long memberId,
                                                         @ModelAttribute PostSearchCondition condition,
                                                         @PageableDefault Pageable pageable,
                                                         HttpServletResponse response){
        Page<PostResponse> page = postService.findPostsByAuthorId(memberId, condition, pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> searchPostByCondition(@LoginMember(required = false) Long memberId,
                                                                 @ModelAttribute PostSearchCondition condition,
                                                                 @PageableDefault Pageable pageable,
                                                                 HttpServletResponse response){
        Page<PostResponse> page = postService.searchPostByCondition(memberId, condition, pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    @PostMapping("/burkInsert")
    public void bulkInsert(){
        postService.bulkCreatePosts();
    }

}
