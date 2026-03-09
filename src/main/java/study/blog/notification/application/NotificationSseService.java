package study.blog.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import study.blog.notification.domain.NotificationType;
import study.blog.notification.infrastructure.persistence.SseEmitterRepository;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter connect(Long memberId){
        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
        sseEmitterRepository.save(memberId, sseEmitter);

        sseEmitter.onCompletion(()->{
           log.info("SSE 연결 종료 - memberId = {}", memberId);
           sseEmitterRepository.delete(memberId);
        });

        sseEmitter.onTimeout(() -> {
            log.info("SSE 타임아웃 - memberId={}", memberId);
            sseEmitter.complete();
            sseEmitterRepository.delete(memberId);
        });

        sseEmitter.onError((e) -> {
            log.warn("SSE 에러 - memberId={}", memberId, e);
            sseEmitter.complete();
            sseEmitterRepository.delete(memberId);
        });

        sendConnectMessage(memberId, sseEmitter);
        return sseEmitter;
    }

    public void send(Long memberId, NotificationType type, Object data) {
        SseEmitter emitter = sseEmitterRepository.get(memberId);

        if (emitter == null) {
            log.info("연결된 SSE 없음 - memberId={}", memberId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(type.name())
                    .data(data));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 - memberId={}", memberId, e);
            sseEmitterRepository.delete(memberId);
            emitter.complete();
        }
    }

    private void sendConnectMessage(Long memberId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected. memberId=" + memberId));
        } catch (IOException e) {
            sseEmitterRepository.delete(memberId);
            emitter.complete();
            throw new IllegalStateException("SSE 연결 초기 메시지 전송 실패", e);
        }
    }
}
