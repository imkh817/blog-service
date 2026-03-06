package study.blog.like.postlike.dto;

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
