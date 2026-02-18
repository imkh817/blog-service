package study.blog.post.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.service.PostService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(Long memberId,
                                                @Valid @RequestBody CreatePostDto createPostDto){
        PostResponse post = postService.createPost(memberId, createPostDto);
        return ApiResponse.success(post);
    }

    @PutMapping
    public ApiResponse<PostResponse> modifyPost(Long memberId,
                                                @Valid @RequestBody UpdatePostDto updatePostDto){
        PostResponse response = postService.modifyPost(memberId, updatePostDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/publish")
    public ApiResponse<PostResponse> changeStatusToPublish(@PathVariable Long postId){
        PostResponse response = postService.changeStatusToPublish(postId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/hide")
    public ApiResponse<PostResponse> changeStatusToHidden(@PathVariable Long postId){
        PostResponse response = postService.changeStatusToHidden(postId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{postId}/delete")
    public ApiResponse<PostResponse> changeStatusToDelete(@PathVariable Long postId){
        PostResponse response = postService.changeStatusToDelete(postId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> findPost(@PathVariable Long postId){
        PostResponse post = postService.findPost(postId);
        return ApiResponse.success(post);
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> searchPostByCondition(@ModelAttribute PostSearchCondition condition,
                                                                 @PageableDefault Pageable pageable,
                                                                 HttpServletResponse response){
        Page<PostResponse> page = postService.searchPostByCondition(condition, pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    @PostMapping("/burkInsert")
    public void bulkInsert(){
        postService.bulkCreatePosts();
    }

}
