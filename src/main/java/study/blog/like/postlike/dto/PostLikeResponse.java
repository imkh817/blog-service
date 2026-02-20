package study.blog.like.postlike.dto;

import study.blog.like.postlike.entity.PostLike;
import study.blog.post.entity.Post;

public record PostLikeResponse(
        Long postId,
        Long likeCount,
        Boolean liked
) {
    public static PostLikeResponse from(Long postId, Long likeCount, Boolean liked) {
        return new PostLikeResponse(
                postId,
                likeCount,
                liked
        );
    }
}
