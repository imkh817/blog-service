package study.blog.post.infrastructure.persistence.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.post.domain.entity.Post;

public interface PostCommandRepository extends JpaRepository<Post, Long>, PostCommandRepositoryCustom {
}
