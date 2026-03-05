package study.blog.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.entity.Comment;
import study.blog.comment.exception.CommentNotFoundException;
import study.blog.comment.repository.query.CommentQueryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryRepository queryRepository;

    public List<CommentViewResponse> findAllCommentWithPaging(Long postId, Pageable pageable) {
        return queryRepository.findAllCommentsWithPaging(postId, pageable);
    }

    public CommentResponse findComment(Long postId, Long commentId) {
        Comment comment = queryRepository.findByPostIdAndId(postId, commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다"));
        return CommentResponse.from(comment);
    }
}
