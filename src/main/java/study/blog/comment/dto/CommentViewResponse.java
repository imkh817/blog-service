package study.blog.comment.dto;

import com.querydsl.core.annotations.QueryProjection;
import study.blog.comment.entity.Comment;
import study.blog.comment.enums.CommentStatus;

import java.time.LocalDateTime;

public record CommentViewResponse(
        Long commentId,
        Long postId,
        Long authorId,
        String authorNickname,
        String content,
        Long parentId,
        CommentStatus status,
        LocalDateTime createdAt
) {
    @QueryProjection
    public CommentViewResponse(Long commentId, Long postId, Long authorId, String authorNickname, String content, Long parentId, CommentStatus status, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.content = content;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
    }
}
