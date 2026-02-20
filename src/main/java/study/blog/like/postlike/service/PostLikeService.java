package study.blog.like.postlike.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.entity.PostLike;
import study.blog.like.postlike.exception.DuplicatePostLikeException;
import study.blog.like.postlike.exception.LikeNotFoundException;
import study.blog.like.postlike.repository.PostLikeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글 좋아요 생성
     */
    @Transactional
    public PostLikeResponse likePost(Long postId, Long memberId) {
        if (postLikeRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw new DuplicatePostLikeException("이미 좋아요를 누른 게시글입니다.");
        }

        postLikeRepository.save(PostLike.createPostLike(postId, memberId));
        Long likeCount = postLikeRepository.countByPostId(postId);
        return PostLikeResponse.from(postId, likeCount, true);
    }

    /**
     * 게시글 좋아요 취소
     */
    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long memberId) {
        if (!postLikeRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw new LikeNotFoundException("좋아요를 누르지 않은 게시글입니다.");
        }

        postLikeRepository.deleteByMemberIdAndPostId(memberId, postId);
        Long likeCount = postLikeRepository.countByPostId(postId);
        return PostLikeResponse.from(postId, likeCount, false);
    }

    /**
     * 게시글 좋아요 조회
     * @param memberId ControllerLayer의 SecurityContext에서 가져온 로그인한 회원의 ID
     * @param postId 조회할 게시글의 ID
     * @return
     */
    public PostLikeResponse findPostLikes(Long memberId, Long postId) {
        Long likeCount = postLikeRepository.countByPostId(postId);

        boolean likedByMe = memberId != null
                && postLikeRepository.existsByMemberIdAndPostId(memberId, postId);

        return PostLikeResponse.from(postId, likeCount, likedByMe);
    }
}
