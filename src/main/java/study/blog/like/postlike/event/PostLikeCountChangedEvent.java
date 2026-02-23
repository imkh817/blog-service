package study.blog.like.postlike.event;

public record PostLikeCountChangedEvent(Long postId, int delta) {
}
