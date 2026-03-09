# Blog - 블로그

---
> 조회수·좋아요 동시성 문제와 실시간 알림 기능을 구현한 백엔드 중심 프로젝트

<br>

# 핵심 설계 및 문제 해결 기록

---
## 아키텍처 & 설계
- [Post - Comment간 연관관계 제거 및 Aggregate를 분리한 이유](docs/post-comment-decoupling.md)
- [동적 정렬 구현 방식의 선택과 이유](docs/dynamic-sort-design.md)


## 성능 최적화
- [Redis를 활용한 좋아요 카운트 최적화](docs/redis-like-count-optimization.md)
- [검색 API 성능 개선: fetch join을 버리고 @BatchSize를 선택한 이유](docs/search-api-batchsize-optimization.md)
- 

## 동시성 및 이벤트 처리
- [중복 요청을 전제로 설계한 좋아요 기능](docs/like-concurrency-design.md)
- [조회수 이벤트를 비동기로 처리할 때 스레드 풀을 설정한 기준](docs/view-count-event-threadpool.md)

## 인증 및 보안
- [Refresh Token Rotation과 블랙리스트 전략을 도입한 이유](docs/refresh-token-rotation-blacklist.md)

## 트러블 슈팅
- [Redis 기반 조회수 처리 중 조회수가 사라진 문제 분석](docs/redis-view-count-loss-issue.md)