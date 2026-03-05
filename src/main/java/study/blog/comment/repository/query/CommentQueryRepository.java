package study.blog.comment.repository.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.comment.entity.Comment;

import java.util.Optional;

public interface CommentQueryRepository extends JpaRepository<Comment, Long>, CommentQueryRepositoryCustom {
    Optional<Comment> findByPostIdAndId(Long postId, Long commentId);
}
