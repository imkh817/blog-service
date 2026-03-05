package study.blog.post.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import study.blog.post.infra.ViewCountService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountEventHandler {

    private final ViewCountService viewCountService;


    /**
     * 게시글 조회 이벤트 처리
     *
     * @Async: 비동기로 처리하여 조회수 증가가 게시글 응답 속도에 영향을 주지 않는다.
     *         (Redis 장애 시에도 게시글 조회는 정상 동작)
     *
     * 처리 순서:
     * 1. 중복 조회 확인 (같은 유저/IP가 10분 내 재조회면 카운트 안함)
     * 2. 중복 아니면 Redis 카운터 증가
     */
    @Async(value = "viewEventExecutor")
    @EventListener
    public void handle(PostViewedEvent event){
        viewCountService.increaseViewCount(event.postId());
    }

}
