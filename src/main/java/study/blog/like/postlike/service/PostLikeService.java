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
import study.blog.like.postlike.repository.PostLikeCountReader;
import study.blog.like.postlike.repository.PostLikeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostLikeCountReader postLikeCountReader;

    /**
     * 게시글 좋아요 생성
     */
    @Transactional
    public PostLikeResponse likePost(Long memberId, Long postId) {
        try {
            postLikeRepository.save(PostLike.createPostLike(postId, memberId));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatePostLikeException("이미 좋아요를 누른 게시글입니다.");
        }

        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, 1));

        // Redis 캐시 기준으로 카운트 반환 (eventual consistency 허용)
        Long likeCount = postLikeCountReader.getLikeCount(postId);

        return PostLikeResponse.from(postId, likeCount, true);
    }

    /**
     * 게시글 좋아요 취소
     */
    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long memberId) {
        int deleted = postLikeRepository.deleteByMemberIdAndPostId(memberId, postId);

        if(deleted == 0){
            throw new LikeNotFoundException("좋아요를 누르지 않은 게시글입니다.");
        }

        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, -1));
        Long likeCount = postLikeCountReader.getLikeCount(postId);
        return PostLikeResponse.from(postId, likeCount, false);
    }
}
