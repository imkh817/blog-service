package study.blog.post.repository.query;

import org.springframework.data.domain.Pageable;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;

import java.util.List;

public interface PostQueryRepositoryCustom {

    List<Post> searchPostsByCondition(PostSearchCondition condition, Pageable pageable);

    long countPostByCondition(PostSearchCondition condition);

    List<Post> searchMemberPosts(Long memberId, PostSearchCondition condition, Pageable pageable);

    long countMemberPosts(Long memberId, PostSearchCondition condition);
}
