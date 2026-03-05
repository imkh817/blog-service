package study.blog.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.service.CommentQueryService;
import study.blog.global.common.dto.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentQueryController {

    private final CommentQueryService queryService;

    /**
     * 특정 게시글의 댓글 목록을 페이징하여 조회한다.
     *
     * @param postId   댓글을 조회할 게시글 ID
     * @param pageable 페이징 정보 (page, size, sort)
     * @return 댓글 목록
     */
    @GetMapping
    public ApiResponse<List<CommentViewResponse>> findAllCommentWithPaging(@PathVariable Long postId, @PageableDefault Pageable pageable) {
        List<CommentViewResponse> comments = queryService.findAllCommentWithPaging(postId, pageable);
        return ApiResponse.success(comments);
    }

    /**
     * 특정 게시글에 속한 댓글을 단건 조회한다.
     *
     * @param postId    게시글 ID
     * @param commentId 댓글 ID
     * @return 댓글 상세 정보
     */
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> findComment(@PathVariable Long postId, @PathVariable Long commentId) {
        CommentResponse comment = queryService.findComment(postId, commentId);
        return ApiResponse.success(comment);
    }
}
