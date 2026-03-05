package study.blog.like.postlike.repository.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.like.postlike.entity.PostLike;

public interface PostLikeQueryRepository extends JpaRepository<PostLike, Long>, PostLikeQueryRepositoryCustom {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
    Long countByPostId(Long postId);

}
