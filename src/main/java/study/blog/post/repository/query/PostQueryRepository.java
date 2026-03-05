package study.blog.post.repository.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.post.entity.Post;

public interface PostQueryRepository extends JpaRepository<Post, Long>, PostQueryRepositoryCustom {
}
