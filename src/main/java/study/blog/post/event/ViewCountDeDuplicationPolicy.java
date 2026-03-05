package study.blog.post.event;

public interface ViewCountDeDuplicationPolicy {

    boolean allow(PostViewedEvent event);

}
