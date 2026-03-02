package study.blog.like.postlike.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.global.lock.DistributedLock;
import study.blog.post.entity.Post;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.repository.PostRepository;

@Component
@RequiredArgsConstructor
public class PostLikeCountUpdater {

    private final PostRepository postRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PostLikeCountChangedEvent event){
        long updatedRow = postRepository.incrementLikeCount(event.postId(), event.delta());

        if(updatedRow == 0){
            throw new PostNotFoundException("게시글을 찾을 수 없습니다.");
        }
    }
}
