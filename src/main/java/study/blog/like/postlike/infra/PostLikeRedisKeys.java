package study.blog.like.postlike.infra;

public final class PostLikeRedisKeys {
    private PostLikeRedisKeys() {}

    public static String postLikeCount(Long postId) {
        return "post:like:count:" + postId;
    }
}
