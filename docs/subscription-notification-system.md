## 👋 SSE 기반 실시간 알림 구현

---

구독 기능을 구현하면서 한 가지 해보고 싶었던 기능이 있었다.

A 사용자가 B를 구독하면 B에게 알림이 바로 뜨는 기능이다.

예를 들어 B가 웹 페이지를 보고 있는 상태라면 “A가 당신을 구독했습니다”라는 알림이 바로 화면에 표시되는 방식이다. 이런 기능을 구현하려면 서버에서 클라이언트로 데이터를 푸시(push) 할 수 있는 방식이 필요하다.

실시간 알림을 구현하는 방법으로는 보통 다음 세 가지가 많이 언급된다.

- Polling
- WebSocket
- SSE(Server-Sent Events)

처음에는 WebSocket을 사용할까도 고민했다.

다만 이번 기능에서는 클라이언트 → 서버로 메시지를 보낼 일이 거의 없었다.

단순히 서버에서 사용자에게 알림을 전달하는 구조였다.

그래서 양방향 통신이 가능한 WebSocket보다는 구현이 단순한 SSE(Server-Sent Events) 방식으로 구현해보기로 했다.

## 구현 방식

---

구독이 발생하면 이벤트를 발행하고 이 이벤트를 받아서 **SSE로 바로 알림을 보내는 방식으로 구현했다**. 전체 흐름은 다음과 같다.

1. A가 B를 구독한다.
2. 구독 정보를 DB에 저장한다.
3. 구독 완료 후 `SubscribedEvent`를 발행한다.
4. 이벤트 리스너가 이를 수신한다.
5. B에게 SSE를 통해 실시간 알림을 전달한다.

구독이 발생했을 때 이벤트를 발행하도록 구현했다.

```java
public SubscriptionResponse subscribe(Long subscriberId, Long targetId){
    try{
        Subscription subscription = Subscription.createSubscription(subscriberId, targetId);
        subscriptionCommandRepository.save(subscription);
    } catch (DataIntegrityViolationException e){
        throw new DuplicateSubscriptionException("이미 구독 중입니다.");
    }

    subscriptionTracker.track(subscriberId, targetId);
    return SubscriptionResponse.of(subscriberId, targetId);
}
```

구독 이벤트 발행은 `SubscriptionTracker`가 담당하도록 했다.

```java
@Component
@RequiredArgsConstructor
public class SubscriptionTracker {

    private final ApplicationEventPublisher publisher;

    public void track(Long subscriberId, Long targetId){
        publisher.publishEvent(new SubscribedEvent(subscriberId, targetId));
    }
}
```

이벤트 리스너에서는 해당 이벤트를 수신한 뒤 SSE 연결을 통해 알림을 전달하도록 구현했다.

```java
@EventListener
public void handle(SubscribedEvent event) {
    notificationSseService.send(
        event.targetId(),
        "subscribed",
        NotificationResponse.of(event.subscriberId(), event.targetId())
    );
}
```

사용자가 웹에 접속하면 `/notifications/subscribe` API를 통해 SSE 연결을 생성하고 서버에서는 사용자별 `SseEmitter`를 저장해두었다가 이벤트 발생 시 알림을 전송하는 방식이다.

간단하게 말하면 “구독 이벤트가 발생하면 바로 SSE로 알림을 보내는 구조”다.

실제로 브라우저를 두 개 띄워서 테스트를 해봤다.

- A 계정으로 로그인한 브라우저
- B 계정으로 로그인한 브라우저

A 계정에서 B를 구독하면 B 화면에서 알림이 바로 뜨는 것을 확인할 수 있었다.

여기까지는 생각했던 대로 잘 동작했다.

![알림 구독 사진](/docs/image9.png)

알림 구독 사진

## 테스트하면서 발견한 문제

---

하지만 테스트를 조금 더 해보면서 한 가지 문제가 보였다.

현재 구조에서는 **알림을 받는 사용자가 웹에 접속 중이어야만 알림을 받을 수 있었다.**

예를 들어 이런 상황이다.

- B가 로그인하지 않은 상태
- 브라우저를 닫은 상태
- 네트워크가 끊어진 상태
- SSE 연결이 없는 상태

이 경우 A가 B를 구독하더라도 **B는 해당 알림이 발생했다는 사실을 전혀 알 수 없다.**

```
  B가 웹을 꺼놓은 상태
          ↓
  A가 구독 → 이벤트 발행 → SseEmitterRepository에서 B 조회
          ↓
  emitter = null → 알림 그냥 무시
          ↓
  B가 나중에 웹을 켜도 해당 알림을 받을 방법이 없음
```

처음에는 “실시간 알림이니까 이 정도면 되는 것 아닌가?”라고 생각했다.

그런데 사용자 입장에서 다시 생각해보니 조금 이상했다.

보통 알림 기능은 내가 접속 중일 때만 보는 것이 아니라 **나중에 접속해도 확인할 수 있어야 한다.**

하지만 현재 구조에서는 사용자가 접속해 있지 않은 순간에 발생한 알림은 그대로 사라진다.

결국 지금 구조는

- **실시간 전송은 가능하지만**
- **알림을 보관하지 못하는 구조**

라는 문제가 있었다.

![로그인하지 않은 상태에서 알람 발생 후 로그인시 화면에 나타나지 않음](/docs/image10.png)

로그인하지 않은 상태에서 알람 발생 후 로그인시 화면에 나타나지 않음

그래서 알림을 단순히 전송하는 구조에서 알림을 저장하는 구조로 변경하기로 했다.

## 해결: Notification 저장 구조 도입

---

기존 구조는 아래와 같다.

```
구독 이벤트 발생
-> SSE 전송
```

하지만 이 방식은 위에서 말했듯이 사용자가 접속해 있을 때만 알림을 받을 수 있는 구조이다.

그래서 알림 시스템을 다음과 같이 변경했다.

```
구독 이벤트 발생
→ Notification 테이블 저장
→ SSE 전송 시도
```

이렇게 하면

- 사용자가 접속 중이면 실시간 알림
- 접속하지 않았으면 나중에 unread 알림 조회

두 요구사항 모두 처리할 수 있다.

### 알람 처리 흐름

구독 이벤트가 발생했을 때의 처리 흐름은 아래와 같이 설계 했다.

```
구독 이벤트 발생
     ↓
Notification 테이블 알림 저장
     ↓
SSE 전송 시도
     ↓
사용자가 접속 중이면 즉시 알람 표시 / 접속 하지 않았으면 unread 알림 조회
```

### 구현

구독 이벤트를 처리하는 리스너에서 알림 저장과 SSE 전송을 함께 처리하도록 구현했다.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribedEventListener {

    private final NotificationCommandService notificationCommandService;
    private final NotificationSseService notificationSseService;
    private final MemberReader memberReader;

    /**
     * 구독 이벤트를 처리한다.
     *
     * 처리 순서
     * 1. notification 테이블에 INSERT (오프라인 상태여도 알림 유실 방지)
     * 2. SSE 전송 시도 (연결 없으면 무시 — 재연결 시 DB에서 조회 가능)
     *
     * 트랜잭션 정책
     * - AFTER_COMMIT: 구독 트랜잭션이 커밋된 이후에만 실행
     * - REQUIRES_NEW: notification 저장 실패 시에도 구독 데이터에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscribedEvent event) {
        log.info("구독 이벤트 수신 - subscriberId={}, targetId={}", event.subscriberId(), event.targetId());

        String subscriberNickName = memberReader.getNickName(event.subscriberId());
        // 1. DB에 알림 저장
        Notification notification = notificationCommandService.create(
                event.targetId(),
                event.subscriberId(),
                NotificationType.SUBSCRIBED,
                subscriberNickName + "님이 구독하였습니다."
        );

        // 2. SSE 전송 (연결 없으면 무시)
        NotificationResponse response = NotificationResponse.of(
                notification.getId(),
                event.subscriberId(),
                event.targetId(),
                notification.getCreatedAt()
        );
        notificationSseService.send(event.targetId(), NotificationType.SUBSCRIBED, response);
    }
}
```

## 또 다른 문제 발생

---

Notification 테이블을 도입하면서 알림 유실 문제는 해결했다.

하지만 구현을 테스트하면서 또 하나의 문제를 발견했다.

현재 코드는 아래의 순서로 실행된다.

```
구독 트랜잭션
-> SubscribedEvent 발행
-> AFTER_COMMIT 이후 이벤트 리스너 실행
-> Notification 저장 (REQUIREDS_NEW 트랜잭션)
-> SSE 전송
```

문제는 SSE 전송 시점이다.

Notification은 REQUIREDS_NEW 트랜잭션 안에서 저장되고 트랜잭션이 커밋되기 전에 SSE가 먼저 전송될 수 있다. 즉 아래와 같은 상황이 발생할 수 있다.

```
       서버                            클라이언트

Notification INSERT (미커밋)
        ↓
SSE 전송 "새 알림 도착"
                                        ↓
                                   unread 알림 조회
                                        ↓
                                    [] 빈 결과
        ↓
REQUIRES_NEW 트랜잭션 커밋
```

이 경우 클라이언트 입장에서는 SSE로 “새 알림 도착” 메시지를 받았지만 바로 알림 목록을 조회하면 아무것도 나오지 않는 상황이 발생한다.

## 해결 : Notification 저장과 SSE 전송 시점 분리

---

이 문제를 해결하기 위해 Notification 저장과 SSE 전송 시점을 분리하기로 했다.

흐름은 아래와 같이 변경됐다.

```java
Notification 저장 -> 트랜잭션 커밋 -> SSE 전송 이벤트 발행 -> SSE 전송
```

즉 **DB 커밋이 완료된 이후에만 SSE 전송이 이루어지도록 구조를 변경했다.**