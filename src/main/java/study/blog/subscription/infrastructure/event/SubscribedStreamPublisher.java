package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribedStreamPublisher {

    static final String STREAM_KEY = "stream:subscribed";

    private final StringRedisTemplate redisTemplate;

    public void publish(Long subscriberId, Long targetId) {
        redisTemplate.opsForStream().add(
                STREAM_KEY,
                Map.of(
                        "subscriberId", String.valueOf(subscriberId),
                        "targetId", String.valueOf(targetId)
                )
        );
        log.info("구독 이벤트 Redis Stream 발행 - subscriberId={}, targetId={}", subscriberId, targetId);
    }
}
