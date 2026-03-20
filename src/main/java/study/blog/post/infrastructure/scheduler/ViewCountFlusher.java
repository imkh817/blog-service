package study.blog.post.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;

/**
 * 처리 정책:
 *   - DB 쓰기 성공 → 트랜잭션 커밋
 *   - DB 쓰기 실패 → Redis 값 복원 후 트랜잭션 롤백 (다음 스케줄에서 재처리)
 *   - DB 쓰기 실패 + Redis 복원 실패 → 예외 로그 기록 (해당 건만 소실, 다른 포스트 무영향)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlusher {

    private final StringRedisTemplate redisTemplate;
    private final PostCommandRepository postCommandRepository;

    @Transactional
    public void flush(String key, String value) {
        try {
            Long postId = extractPostId(key);
            long viewCount = Long.parseLong(value);
            postCommandRepository.incrementViewCount(postId, viewCount);
        } catch (Exception e) {
            log.error("조회수 DB 반영 실패 - key: {}, count: {} 복원 시도", key, value, e);
            try {
                redisTemplate.opsForValue().increment(key, Long.parseLong(value));
            } catch (Exception redisEx) {
                log.error("조회수 Redis 복원 실패 - key: {}, count: {} 데이터 소실 발생", key, value, redisEx);
            }
        }
    }

    private Long extractPostId(String key) {
        return Long.parseLong(key.split(":")[3]);
    }
}
