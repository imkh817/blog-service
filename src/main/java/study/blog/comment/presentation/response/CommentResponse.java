package study.blog.comment.presentation.response;

import study.blog.comment.domain.entity.Comment;
import study.blog.comment.domain.CommentStatus;

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
