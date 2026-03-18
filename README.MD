# Blog Platform - Spring Boot 기반 백엔드 블로그 서비스

> 조회수·좋아요 동시성 문제와 실시간 알림 기능을 구현한 백엔드 중심 프로젝트


## Demo
> 🌐 **Live Demo** <br>
> https://my-devlog.duckdns.org


## 핵심 설계 및 문제 해결 기록

### 아키텍처 & 설계
DDD 원칙을 기반으로 구조적 결정을 내린 이유를 정리했습니다.

- [게시글과 댓글, 왜 굳이 분리했을까](https://imkh817.github.io/posts/%EA%B2%8C%EC%8B%9C%EA%B8%80%EA%B3%BC-%EB%8C%93%EA%B8%80,-%EC%99%9C-%EA%B5%B3%EC%9D%B4-%EB%B6%84%EB%A6%AC%ED%96%88%EC%9D%84%EA%B9%8C/)
- [정렬 기준이 바뀔 때마다 코드를 고쳐야 할까](https://imkh817.github.io/posts/%EC%A0%95%EB%A0%AC-%EA%B8%B0%EC%A4%80%EC%9D%B4-%EB%B0%94%EB%80%94-%EB%95%8C%EB%A7%88%EB%8B%A4-%EC%BD%94%EB%93%9C%EB%A5%BC-%EA%B3%A0%EC%B3%90%EC%95%BC-%ED%95%A0%EA%B9%8C/)

### 성능 최적화
쿼리 튜닝과 캐시 전략으로 성능을 개선한 과정을 기록했습니다.

- [[1탄] 좋아요 누를 때마다 DB를 찌르고 있었다](https://imkh817.github.io/posts/%EC%A2%8B%EC%95%84%EC%9A%94%EB%A5%BC-%EB%88%84%EB%A5%BC%EB%95%8C%EB%A7%88%EB%8B%A4-DB%EB%A5%BC-%EC%B0%8C%EB%A5%B4%EA%B3%A0-%EC%9E%88%EC%97%88%EB%8B%A4/)
- [[2탄] 좋아요 수, 굳이 따로 집계해야 할까](https://imkh817.github.io/posts/%EC%A2%8B%EC%95%84%EC%9A%94-%EC%88%98,-%EA%B5%B3%EC%9D%B4-%EB%94%B0%EB%A1%9C-%EC%A7%91%EA%B3%84%ED%95%B4%EC%95%BC-%ED%95%A0%EA%B9%8C/)
- [검색이 느린 이유가 fetch join 때문이었다](https://imkh817.github.io/posts/%EA%B2%80%EC%83%89%EC%9D%B4-%EB%8A%90%EB%A6%B0-%EC%9D%B4%EC%9C%A0%EA%B0%80-fetch-join-%EB%95%8C%EB%AC%B8%EC%9D%B4%EC%97%88%EB%8B%A4/)

### 동시성 처리
동시 요청이 들어왔을 때 발생할 수 있는 문제를 분석하고 해결한 과정을 기록했습니다.

- [좋아요를 동시에 두 번 누르면 어떻게 될까](https://imkh817.github.io/posts/%EC%A2%8B%EC%95%84%EC%9A%94%EB%A5%BC-%EB%8F%99%EC%8B%9C%EC%97%90-%EB%91%90%EB%B2%88-%EB%88%84%EB%A5%B4%EB%A9%B4-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%90%A0%EA%B9%8C/)
- [스레드는 몇 개면 충분할까](https://imkh817.github.io/posts/%EC%8A%A4%EB%A0%88%EB%93%9C%EB%8A%94-%EB%AA%87%EA%B0%9C%EB%A9%B4-%EC%B6%A9%EB%B6%84%ED%95%A0%EA%B9%8C/)

### 실시간 알림 & 메시징
SSE와 Redis Streams로 멀티인스턴스 알림 구조를 개선한 과정을 기록했습니다.
- [실시간 알림인데 왜 늦게 올까](https://imkh817.github.io/posts/%EC%8B%A4%EC%8B%9C%EA%B0%84-%EC%95%8C%EB%A6%BC%EC%9D%B8%EB%8D%B0-%EC%99%9C-%EB%8A%A6%EA%B2%8C-%EC%98%AC%EA%B9%8C/)
- [[1탄] 서버가 여러 대면 알림은 누가 보내야 할까](https://imkh817.github.io/posts/1%ED%83%84-%EB%A9%80%ED%8B%B0%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4-%ED%99%98%EA%B2%BD%EC%9D%84-%EA%B3%A0%EB%A0%A4%ED%95%9C-%EA%B5%AC%EB%8F%85-%EC%95%8C%EB%A6%BC-%EA%B5%AC%EC%A1%B0-%EA%B0%9C%EC%84%A0/)
- [[2탄] Consumer가 죽으면 메시지는 어디 있을까](https://imkh817.github.io/posts/2%ED%83%84-Consumer%EA%B0%80-%EC%A3%BD%EC%9C%BC%EB%A9%B4-%EB%A9%94%EC%8B%9C%EC%A7%80%EB%8A%94-%EC%96%B4%EB%94%94-%EC%9E%88%EC%9D%84%EA%B9%8C/)
- [[3탄] 알림이 어떤 서버에 붙었느냐에 따라 안 오고 있었다](https://imkh817.github.io/posts/3%ED%83%84-SSE-%EC%95%8C%EB%A6%BC,-%EC%99%9C-%EA%B0%80%EB%81%94-%EC%95%88-%EC%98%AC%EA%B9%8C-Redis-PubSub%EC%9C%BC%EB%A1%9C-%EB%A9%80%ED%8B%B0-%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4-%EB%B8%8C%EB%A1%9C%EB%93%9C%EC%BA%90%EC%8A%A4%ED%8A%B8-%EA%B5%AC%ED%98%84/)

### 인증 & 보안
JWT 기반 인증과 토큰 탈취 대응 전략을 정리했습니다.

- [토큰을 훔쳐도 못 쓰게 만들 수 있을까](https://imkh817.github.io/posts/%ED%86%A0%ED%81%B0%EC%9D%84-%ED%9B%94%EC%B3%90%EB%8F%84-%EB%AA%BB%EC%93%B0%EA%B2%8C-%EB%A7%8C%EB%93%A4%EC%88%98%EC%9E%88%EC%9D%84%EA%B9%8C/)

### 트러블슈팅
예상치 못한 버그의 원인을 분석하고 해결한 과정을 기록했습니다.

- [조회수가 갑자기 사라졌다](https://imkh817.github.io/posts/%EC%A2%8B%EC%95%84%EC%9A%94%EC%88%98%EA%B0%80-%EA%B0%91%EC%9E%90%EA%B8%B0-%EC%82%AC%EB%9D%BC%EC%A1%8C%EB%8B%A4/)
- [Security 필터에서 던진 예외가 왜 안 잡힐까](https://imkh817.github.io/posts/Security-Filter%EC%97%90%EC%84%9C-%EB%8D%98%EC%A7%84-%EC%98%88%EC%99%B8%EA%B0%80-%EC%99%9C-%EC%95%88%EC%9E%A1%ED%9E%90%EA%B9%8C/)
- [분명히 접근을 막았는데 왜 엉뚱한 곳으로 튈까](https://imkh817.github.io/posts/%EB%B6%84%EB%AA%85%ED%9E%88-%EC%A0%91%EA%B7%BC%EC%9D%84-%EB%A7%89%EC%95%98%EB%8A%94%EB%8D%B0-%EC%99%9C-%EC%97%89%EB%9A%B1%ED%95%9C-%EA%B3%B3%EC%9C%BC%EB%A1%9C-%ED%8A%88%EA%B9%8C/)

## 기술 스택

| 영역 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Backend** | Spring Boot 3.5, Spring Security, Spring Data JPA, QueryDSL |
| **Database** | MySQL 8.0, Redis 7.2 |
| **Auth** | JWT, Refresh Token Rotation |
| **Infra** | AWS S3, Docker Compose |
| **Frontend** | Vue.js 3, Pinia, TailwindCSS |
| **Test** | JUnit 5, Testcontainers |


## 아키텍처

![architecture.jpg](docs/image/architecture.jpg)
