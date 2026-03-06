package study.blog.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.domain.entity.Comment;
import study.blog.comment.infrastructure.persistence.command.CommentCommandRepository;
import study.blog.comment.presentation.request.CreateCommentRequest;
import study.blog.comment.presentation.response.CommentResponse;

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
