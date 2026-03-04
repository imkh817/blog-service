package study.blog.post.repository;

import org.springframework.data.domain.Pageable;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPostsByCondition(Long memberId, PostSearchCondition condition, Pageable pageable);

    long countPostByCondition(PostSearchCondition condition);

    long incrementLikeCount(Long postId, int delta);

    long incrementViewCount(Long postId, long viewCount);

    List<Post> searchMemberPosts(Long memberId, PostSearchCondition condition, Pageable pageable);

    long countMemberPosts(Long memberId, PostSearchCondition condition);
}
