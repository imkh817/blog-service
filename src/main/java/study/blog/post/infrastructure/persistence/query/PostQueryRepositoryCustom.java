package study.blog.post.infrastructure.persistence.query;

import org.springframework.data.domain.Pageable;
import study.blog.post.domain.entity.Post;
import study.blog.post.presentation.requset.PostSearchCondition;

import java.util.List;

public interface PostQueryRepositoryCustom {

    List<Post> searchPostsByCondition(PostSearchCondition condition, Pageable pageable);

    long countPostByCondition(PostSearchCondition condition);

    List<Post> searchMemberPosts(Long memberId, PostSearchCondition condition, Pageable pageable);

    long countMemberPosts(Long memberId, PostSearchCondition condition);

    List<Post> findMainPosts(Pageable pageable);

    long countMainPosts();
}
