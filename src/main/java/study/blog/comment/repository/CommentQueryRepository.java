package study.blog.comment.repository;

import org.springframework.data.domain.Pageable;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.entity.Comment;

import java.util.List;

public interface CommentQueryRepository{
    public List<CommentViewResponse> findAllCommentsWithPaging(Long postId, Pageable pageable);
}
