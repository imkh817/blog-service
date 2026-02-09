package study.blog.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
