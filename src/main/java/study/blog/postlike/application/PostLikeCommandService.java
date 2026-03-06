package study.blog.postlike.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.postlike.domain.entity.PostLike;
import study.blog.postlike.domain.exception.DuplicatePostLikeException;
import study.blog.postlike.domain.exception.LikeNotFoundException;
import study.blog.postlike.infrastructure.persistence.command.PostLikeCommandRepository;
import study.blog.postlike.infrastructure.persistence.query.PostLikeQueryRepository;
import study.blog.postlike.presentation.response.PostLikeResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeCommandService {

    private final PostLikeQueryRepository postLikeQueryRepository;
    private final PostLikeCommandRepository postLikeCommandRepository;
    private final LikeCountTracker likeCountTracker;

    /**
     * 게시글 좋아요를 생성한다.
     *
     * 비즈니스 규칙
     * - 동일 사용자는 같은 게시글에 좋아요를 한 번만 누를 수 있다.
     *
     * 좋아요 처리 정책
     * - 좋아요/취소 직후 응답값의 likeCount는 DB count(*) 조회를 사용하여 정확성을 우선한다.
     * - 목록 조회 정렬은 Post.likeCount 비정규화 컬럼을 사용하여 성능을 우선한다.
     * - Post.likeCount는 AFTER_COMMIT 이벤트를 통해 비동기로 반영된다.
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long memberId) {
        try {
            postLikeCommandRepository.save(PostLike.createPostLike(postId, memberId));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatePostLikeException("이미 좋아요를 누른 게시글입니다.");
        }

        long likeCount = postLikeQueryRepository.countByPostId(postId);

        likeCountTracker.track(postId, 1);

        return PostLikeResponse.from(postId, likeCount, true);
    }

    /**
     * 게시글 좋아요를 취소한다.
     *
     * 비즈니스 규칙
     * - 좋아요를 누르지 않은 게시글은 취소할 수 없다.
     *
     * 좋아요 처리 정책
     * - 좋아요/취소 직후 응답값의 likeCount는 DB count(*) 조회를 사용하여 정확성을 우선한다.
     * - 목록 조회 정렬은 Post.likeCount 비정규화 컬럼을 사용하여 성능을 우선한다.
     * - Post.likeCount는 AFTER_COMMIT 이벤트를 통해 비동기로 반영된다.
     */
    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long memberId) {

        int deleted = postLikeCommandRepository.deleteByMemberIdAndPostId(memberId, postId);

        if (deleted == 0) {
            throw new LikeNotFoundException("좋아요를 누르지 않은 게시글입니다.");
        }

        long likeCount = postLikeQueryRepository.countByPostId(postId);

        likeCountTracker.track(postId, -1);

        return PostLikeResponse.from(postId, likeCount, false);
    }
}
