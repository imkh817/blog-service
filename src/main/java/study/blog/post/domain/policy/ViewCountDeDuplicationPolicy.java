package study.blog.post.domain.policy;

import study.blog.post.domain.event.PostViewedEvent;

public interface ViewCountDeDuplicationPolicy {

    boolean allow(PostViewedEvent event);

}
