package study.blog.comment.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.comment.entity.Comment;

public interface CommentCommandRepository extends JpaRepository<Comment, Long> , CommentCommandRepositoryCustom{
}
