package study.blog.like.postlike.repository.query;

import java.util.List;

public interface PostLikeQueryRepositoryCustom {
    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
}
