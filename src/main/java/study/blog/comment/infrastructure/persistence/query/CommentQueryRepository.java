package study.blog.comment.infrastructure.persistence.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.comment.domain.entity.Comment;

import java.util.Optional;

public interface CommentQueryRepository extends JpaRepository<Comment, Long>, CommentQueryRepositoryCustom {
    Optional<Comment> findByPostIdAndId(Long postId, Long commentId);
}
