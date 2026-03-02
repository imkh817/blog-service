package study.blog.like.postlike.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.entity.PostLike;
import study.blog.like.postlike.event.PostLikeCountChangedEvent;
import study.blog.like.postlike.exception.DuplicatePostLikeException;
import study.blog.like.postlike.exception.LikeNotFoundException;
import study.blog.like.postlike.repository.PostLikeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 게시글 좋아요 생성
     * 트랜잭션 내에서 DB count를 직접 읽어 반환 (Redis는 AFTER_COMMIT에 비동기로 반영됨)
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long memberId) {
        try {
            postLikeRepository.save(PostLike.createPostLike(postId, memberId));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatePostLikeException("이미 좋아요를 누른 게시글입니다.");
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        // 이벤트 발생 -> likeCount update
        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, 1));

        return PostLikeResponse.from(postId, likeCount, true);
    }

    /**
     * 게시글 좋아요 취소
     * 트랜잭션 내에서 DB count를 직접 읽어 반환 (Redis는 AFTER_COMMIT에 비동기로 반영됨)
     */
    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long memberId) {

        int deleted = postLikeRepository.deleteByMemberIdAndPostId(memberId, postId);

        if (deleted == 0) {
            throw new LikeNotFoundException("좋아요를 누르지 않은 게시글입니다.");
        }


        long likeCount = postLikeRepository.countByPostId(postId);
        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, -1));
        return PostLikeResponse.from(postId, likeCount, false);
    }
}
