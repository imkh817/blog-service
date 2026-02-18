package study.blog.comment.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.entity.Comment;
import study.blog.comment.exception.CommentNotFoundException;
import study.blog.comment.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse createComment(Long postId, Long memberId,
                                         @Valid CreateCommentRequest request) {
        Comment comment = Comment.createComment(postId, memberId, request.content(), request.parentId());
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    public List<CommentResponse> findAllCommentWithPaging(Long postId, Pageable pageable) {
        List<Comment> allCommentsWithPaging = commentRepository.findAllCommentsWithPaging(postId, pageable);
        return allCommentsWithPaging.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse findComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findByPostIdAndId(postId, commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다"));
        return CommentResponse.from(comment);
    }
}
