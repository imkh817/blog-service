package study.blog.postlike.infrastructure.persistence.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PostLikeReader {

    private final PostLikeQueryRepository queryRepository;

    public Set<Long> findLikedPostIds(Long memberId, List<Long> postIds){
        return (memberId == null || postIds.isEmpty())
                ? Set.of()
                : new HashSet<>(queryRepository.findPostIdByMemberIdAndPostIdIn(memberId, postIds));
    }

    public boolean hasLiked(Long memberId, Long postId){
        return queryRepository.existsByMemberIdAndPostId(memberId, postId);
    }
}
