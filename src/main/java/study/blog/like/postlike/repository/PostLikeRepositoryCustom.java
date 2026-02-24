package study.blog.like.postlike.repository;

import java.util.List;

public interface PostLikeRepositoryCustom {

    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
}
