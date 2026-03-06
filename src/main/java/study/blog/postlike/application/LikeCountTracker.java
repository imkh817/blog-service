package study.blog.postlike.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import study.blog.postlike.domain.event.PostLikeCountChangedEvent;

@Component
@RequiredArgsConstructor
public class LikeCountTracker {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 게시글 좋아요 수 변경 이벤트를 발행한다.
     *
     * 좋아요 생성/취소 시 likeCount 변경 이벤트(PostLikeCountChangedEvent)를 발행한다.
     * 실제 Post.likeCount 반영은 AFTER_COMMIT 이벤트 리스너에서 처리된다.
     */
    public void track(Long postId, int delta){
        eventPublisher.publishEvent(new PostLikeCountChangedEvent(postId, delta));
    }
}
