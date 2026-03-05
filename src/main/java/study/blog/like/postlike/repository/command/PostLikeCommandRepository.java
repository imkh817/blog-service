package study.blog.like.postlike.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.like.postlike.entity.PostLike;

public interface PostLikeCommandRepository extends JpaRepository<PostLike, Long>, PostLikeCommandRepositoryCustom {
    int deleteByMemberIdAndPostId(Long memberId, Long postId);
}
