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
     * 좋아요/취소 직후 응답값: post_like Count 집계 계산 (정확성 우선)
     * 목록 조회 정렬: Post.likeCount 비정규화 컬럼 사용 (성능 우선)
     * Post.likeCount는 AFTER_COMMIT 이후 이벤트로 비동기 반영
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long memberId) {
        try {
            postLikeRepository.save(PostLike.createPostLike(postId, memberId));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatePostLikeException("이미 좋아요를 누른 게시글입니다.");
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, 1));

        return PostLikeResponse.from(postId, likeCount, true);
    }

    /**
     * 게시글 좋아요 취소
     * 좋아요/취소 직후 응답값: post_like Count 집계 계산 (정확성 우선)
     * 목록 조회 정렬: Post.likeCount 비정규화 컬럼 사용 (성능 우선)
     * Post.likeCount는 AFTER_COMMIT 이후 이벤트로 비동기 반영
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
