package study.blog.like.postlike.repository;

import java.util.List;
import java.util.Map;

public interface PostLikeCountReader {
    Map<Long, Long> getLikeCounts(List<Long> postIds);

    Long getLikeCount(Long postId);
}
