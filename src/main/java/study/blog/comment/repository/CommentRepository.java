package study.blog.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.comment.entity.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByPostIdAndId(Long postId, Long commentId);
}
