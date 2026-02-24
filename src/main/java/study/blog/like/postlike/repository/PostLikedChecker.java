package study.blog.like.postlike.repository;

import java.util.List;

public interface PostLikedChecker {
    List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);
}
