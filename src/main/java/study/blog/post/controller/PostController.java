package study.blog.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.dto.ApiResponse;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.service.PostService;

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
}
