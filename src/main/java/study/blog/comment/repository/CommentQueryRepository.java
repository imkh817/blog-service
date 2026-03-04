package study.blog.comment.repository;

import org.springframework.data.domain.Pageable;
import study.blog.comment.dto.CommentViewResponse;

import java.util.List;
import java.util.Map;

public interface CommentQueryRepository{
    public List<CommentViewResponse> findAllCommentsWithPaging(Long postId, Pageable pageable);

    Map<Long, Long> countCommentByPostIds(List<Long> postIds);

    long countCommentByPostId(Long postId);
}
