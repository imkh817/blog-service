package study.blog.postlike.domain.event;

public record PostLikeCountChangedEvent(
        Long postId,
        int delta
)
{
}
