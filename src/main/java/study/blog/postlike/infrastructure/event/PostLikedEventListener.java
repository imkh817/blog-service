package study.blog.postlike.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.post.domain.exception.PostNotFoundException;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;
import study.blog.postlike.domain.event.PostLikeCountChangedEvent;

@Component
@RequiredArgsConstructor
public class PostLikedEventListener {

    private final PostCommandRepository postCommandRepository;

    /**
     * 게시글 좋아요 수 변경 이벤트를 처리한다.
     *
     * 좋아요 생성/취소 시 발행된 PostLikeCountChangedEvent를 수신하여
     * Post.likeCount 비정규화 컬럼을 업데이트한다.
     *
     * 처리 정책
     * - AFTER_COMMIT 단계에서 실행하여 좋아요 저장 트랜잭션이 성공적으로 커밋된 이후에만 반영한다.
     * - REQUIRES_NEW 트랜잭션을 사용하여 좋아요 처리 트랜잭션과 분리된 독립적인 트랜잭션으로 실행한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PostLikeCountChangedEvent event){
        long updatedRow = postCommandRepository.incrementLikeCount(event.postId(), event.delta());

        if(updatedRow == 0){
            throw new PostNotFoundException("게시글을 찾을 수 없습니다.");
        }
    }
}
