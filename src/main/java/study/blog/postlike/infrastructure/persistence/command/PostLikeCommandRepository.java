package study.blog.postlike.infrastructure.persistence.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.postlike.domain.entity.PostLike;

public interface PostLikeCommandRepository extends JpaRepository<PostLike, Long>, PostLikeCommandRepositoryCustom {
    int deleteByMemberIdAndPostId(Long memberId, Long postId);
}
