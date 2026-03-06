package study.blog.postlike.infrastructure.persistence.query;

import java.util.List;

public interface PostLikeQueryRepositoryCustom {
    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
}
