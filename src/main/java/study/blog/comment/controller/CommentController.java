package study.blog.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.service.CommentService;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;

import java.util.List;


@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponse> createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request, @LoginMember Long memberId) {
        CommentResponse comment = commentService.createComment(postId, memberId, request);
        return ApiResponse.success(comment);
    }

    @GetMapping
    public ApiResponse<List<CommentResponse>> findAllCommentWithPaging(@PathVariable Long postId, @PageableDefault Pageable pageable) {
        List<CommentResponse> comments = commentService.findAllCommentWithPaging(postId, pageable);
        return ApiResponse.success(comments);
    }

    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> findComment(@PathVariable Long postId, @PathVariable Long commentId) {
        CommentResponse comment = commentService.findComment(postId, commentId);
        return ApiResponse.success(comment);
    }
}
