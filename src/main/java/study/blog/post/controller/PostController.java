package study.blog.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.dto.ApiResponse;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.enums.PostStatus;
import study.blog.post.service.PostService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(@RequestParam Long authorId,
                                                @Valid @RequestBody CreatePostDto createPostDto){
        PostResponse post = postService.createPost(authorId, createPostDto);
        return ApiResponse.success(post);
    }

    @PutMapping
    public ApiResponse<PostResponse> modifyPost(@RequestParam Long authorId,
                                                @Valid @RequestBody UpdatePostDto updatePostDto){
        PostResponse response = postService.modifyPost(authorId, updatePostDto);
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
                                                                 @PageableDefault Pageable pageable){
        List<PostResponse> postResponses = postService.searchPostByCondition(condition, pageable);
        return ApiResponse.success(postResponses);
    }

    @PostMapping("/burkInsert")
    public void bulkInsert(){
        postService.bulkCreatePosts();
    }

}
