package study.blog.post.repository;

import org.springframework.data.domain.Pageable;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPostByCondition(PostSearchCondition condition, Pageable pageable);

    long countPostByCondition(PostSearchCondition condition);
}
