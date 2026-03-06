package study.blog.comment.infrastructure.persistence.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.comment.domain.entity.Comment;

public interface CommentCommandRepository extends JpaRepository<Comment, Long> , CommentCommandRepositoryCustom{
}
