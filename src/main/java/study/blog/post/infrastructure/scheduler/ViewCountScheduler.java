package study.blog.post.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final StringRedisTemplate redisTemplate;
    private final ViewCountFlusher viewCountFlusher;

    /**
     * Redis에 누적된 게시글 조회수를 주기적으로 DB에 반영하는 스케줄러.
     *
     * 조회수 증가 요청은 Redis에 먼저 누적되고,
     * 해당 스케줄러가 일정 주기로 Redis 값을 DB(Post.viewCount)에 합산한다.
     *
     * 처리 정책
     * - Redis key: post:view:count:{postId}
     * - 스케줄 주기: 5분
     * - DB 반영 시 원자적 증가
     * - 트랜잭션 범위: 포스트 1건 단위 (ViewCountFlusher 위임)
     *
     * 트레이드 오프
     * - redisTemplate.keys("post:view:count:*") 로 전체 키를 조회한다.
     * - 성능에 영향을 줄 수 있으나, 조회수 데이터는 5분마다 flush 되며
     *   키 개수가 제한적이기 때문에 현재 구조에서는 큰 부하가 없다고 판단하였다.
     *
     * 예외 처리
     * - DB 반영 실패 시 Redis 값을 복원하여 다음 스케줄에서 재처리한다.
     * - 한 포스트의 실패가 다른 포스트 처리에 영향을 주지 않는다.
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void flushViewCountToDB(){
        Set<String> keys = redisTemplate.keys("post:view:count:*");

        if(CollectionUtils.isEmpty(keys)) return;

        for(String key : keys){
            String value = redisTemplate.opsForValue().getAndDelete(key);
            if(value == null) continue;

            viewCountFlusher.flush(key, value);
        }
    }
}
