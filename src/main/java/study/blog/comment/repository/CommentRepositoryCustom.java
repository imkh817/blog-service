package study.blog.comment.repository;

import org.springframework.data.domain.Pageable;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {

    public List<Comment> findAllCommentsWithPaging(Long postId, Pageable pageable);
}
