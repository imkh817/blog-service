package study.blog.comment.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.dto.CommentResponse;
import study.blog.comment.dto.CreateCommentRequest;
import study.blog.comment.entity.Comment;
import study.blog.comment.repository.command.CommentCommandRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentCommandRepository commentCommandRepository;

    public CommentResponse createComment(Long postId, Long memberId,
                                         CreateCommentRequest request) {
        Comment comment = Comment.createComment(postId, memberId, request.content(), request.parentId());
        commentCommandRepository.save(comment);
        return CommentResponse.from(comment);
    }
}
