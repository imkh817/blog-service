package study.blog.post.infrastructure.persistence.command;

public interface PostCommandRepositoryCustom {
    long incrementViewCount(Long postId, long viewCount);
    long incrementLikeCount(Long postId, int delta);
}
