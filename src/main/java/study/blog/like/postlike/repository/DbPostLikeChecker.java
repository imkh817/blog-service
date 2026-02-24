package study.blog.like.postlike.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DbPostLikeChecker implements PostLikedChecker{

    private final PostLikeRepository postLikeRepository;

    @Override
    public List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds){
        return postLikeRepository.findPostIdByMemberIdAndPostIdIn(memberId, postIds);
    }
}
