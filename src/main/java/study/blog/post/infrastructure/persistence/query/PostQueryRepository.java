package study.blog.post.infrastructure.persistence.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.post.domain.entity.Post;

public interface PostQueryRepository extends JpaRepository<Post, Long>, PostQueryRepositoryCustom {
}
