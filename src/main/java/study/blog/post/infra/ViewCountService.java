package study.blog.post.infra;

public interface ViewCountService {
    public boolean isDuplicated(Long postId, String identifier);

    public void increaseViewCount(Long postId);
}
