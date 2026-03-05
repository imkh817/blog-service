package study.blog.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.service.CommentCommandService;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentCommandController {

    private final CommentCommandService commandService;

    /**
     * 특정 게시글에 댓글을 작성한다.
     *
     * @param postId   댓글을 작성할 게시글 ID
     * @param request  댓글 생성 요청 데이터
     * @param memberId 댓글 작성자 ID (로그인 사용자)
     * @return 생성된 댓글 정보
     */
    @PostMapping
    public ApiResponse<CommentResponse> createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request, @LoginMember Long memberId) {
        CommentResponse comment = commandService.createComment(postId, memberId, request);
        return ApiResponse.success(comment);
    }
}
