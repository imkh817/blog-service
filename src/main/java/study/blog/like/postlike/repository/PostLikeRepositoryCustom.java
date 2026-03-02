package study.blog.like.postlike.repository;

import java.util.List;
import java.util.Map;

public interface PostLikeRepositoryCustom {

    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
    Map<Long, Long> countByPostIds(List<Long> postIds);
}
