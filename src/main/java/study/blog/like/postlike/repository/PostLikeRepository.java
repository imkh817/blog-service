package study.blog.like.postlike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.like.postlike.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Long countByMemberId(Long memberId);
    Long countByPostId(Long postId);

    void deleteByMemberIdAndPostId(Long memberId, Long postId);
}
