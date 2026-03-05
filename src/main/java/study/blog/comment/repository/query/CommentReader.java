package study.blog.comment.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CommentReader {
    private final CommentQueryRepository queryRepository;

    /**
     * 게시글 목록에 대한 댓글 수를 조회한다.
     *
     * 게시글 ID → 댓글 수 형태의 Map으로 반환한다.
     * 이를 통해 게시글마다 댓글 수를 조회하는 N+1 문제를 방지한다.
     *
     * @param postIds 게시글 ID 목록
     * @return 게시글 ID → 댓글 수 Map
     */
    public Map<Long, Long> getCommentCounts(List<Long> postIds){
        if(postIds.isEmpty()){
            return Map.of();
        }

        return queryRepository.countCommentByPostIds(postIds);
    }

    /**
     * 특정 게시글의 댓글 수를 조회한다.
     *
     * @param postId 게시글 ID
     * @return 댓글 수
     */
    public long getCommentCount(Long postId){
        return queryRepository.countCommentByPostId(postId);
    }
}
