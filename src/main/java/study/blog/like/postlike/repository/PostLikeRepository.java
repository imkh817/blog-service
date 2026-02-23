package study.blog.like.postlike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.like.postlike.entity.PostLike;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Long countByMemberId(Long memberId);
    Long countByPostId(Long postId);

    int deleteByMemberIdAndPostId(Long memberId, Long postId);

    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
}