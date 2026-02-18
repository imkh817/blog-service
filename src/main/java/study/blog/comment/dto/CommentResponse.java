package study.blog.comment.dto;

import study.blog.comment.entity.Comment;
import study.blog.comment.enums.CommentStatus;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long authorId,
        String content,
        Long parentId,
        CommentStatus status
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getContent(),
                comment.getParentId(),
                comment.getStatus()
        );
    }
}
