package study.blog.post.application;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import study.blog.post.domain.event.PostViewedEvent;
import study.blog.post.domain.policy.ViewCountDeDuplicationPolicy;

@Component
@RequiredArgsConstructor
public class PostViewTracker {
    private final ApplicationEventPublisher eventPublisher;
    private final ViewCountDeDuplicationPolicy policy;

    /**
     * 게시글 조회 이벤트 발행
     *
     * 조회 요청 시 조회수 증가 로직을 동기적으로 처리하지 않고 이벤트로 발행하여 비동기 처리하도록 한다.
     * 조회수 증가 정책(IP, 사용자 중복 체크 등)은 ViewCountDeDuplicationPolicy 에서 검사한다.
     */
    public void track(Long memberId, Long postId, HttpServletRequest request){
        String ip = extractIp(request);
        PostViewedEvent event = new PostViewedEvent(memberId, postId, ip);

        if(policy.allow(event)){
            eventPublisher.publishEvent(event);
        }

    }

    private String extractIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }
}
