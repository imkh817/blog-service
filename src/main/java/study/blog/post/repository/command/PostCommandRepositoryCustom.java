package study.blog.post.repository.command;

public interface PostCommandRepositoryCustom {
    long incrementViewCount(Long postId, long viewCount);
    long incrementLikeCount(Long postId, int delta);
}
