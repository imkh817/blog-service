package study.blog.post.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.post.entity.Post;

public interface PostCommandRepository extends JpaRepository<Post, Long>, PostCommandRepositoryCustom {
}
