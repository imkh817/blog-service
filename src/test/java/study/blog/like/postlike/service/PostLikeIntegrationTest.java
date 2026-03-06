package study.blog.like.postlike.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import study.blog.global.IntegrationTestSupport;
import study.blog.postlike.presentation.response.PostLikeResponse;
import study.blog.postlike.infrastructure.event.PostLikedEventListener;
import study.blog.postlike.domain.exception.DuplicatePostLikeException;
import study.blog.postlike.infrastructure.persistence.query.PostLikeQueryRepository;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;
import study.blog.postlike.application.PostLikeCommandService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class PostLikeIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    private PostLikedEventListener postLikedEventListener;

    @Autowired
    private PostLikeCommandService postLikeCommandService;

    @Autowired
    private PostLikeQueryRepository postLikeQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * PostLike 테이블은 post, member 테이블과 FK 제약이 없으므로 실제 Post/Member 레코드 없이 ID만으로 테스트 가능하다.
     * 동시성 테스트는 각 스레드가 독립적인 트랜잭션을 생성한다.
     * 단, @Transactional 테스트 컨텍스트에서 setUp 데이터가 커밋되지 않으므로
     * 동시성 테스트에서는 테스트 메서드 내에서 직접 데이터를 세팅한다.
     */
    @Test
    @DisplayName("100개의 동시 요청이 들어와도 게시글 좋아요가 정확히 처리된다")
    void 게시글_좋아요_동시성_테스트() throws InterruptedException {
        // given
        Long postId = 1L;
        List<Long> memberIds = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Member member = memberRepository.save(
                    Member.createMember("test" + i + "@naver.com", "password" + i, "nickname" + i)
            );
            memberIds.add(member.getId());
        }

        // when - 각 스레드는 독립적인 트랜잭션에서 likePost를 호출한다
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    // likePost(postId, memberId) - postId가 첫 번째 인자
                    postLikeCommandService.likePost(postId, memberId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // then - 100개의 고유한 (postId, memberId) 조합이 저장되어야 한다
        Long count = postLikeQueryRepository.countByPostId(postId);
        assertThat(count).isEqualTo(100L);
    }

    @Test
    @DisplayName("동일한 회원이 같은 게시글에 좋아요를 두 번 누르면 두 번째 시도에서 예외가 발생한다")
    void 중복_좋아요_예외_발생() {
        // given
        Long postId = 100L;
        Long memberId = 200L;

        // when
        postLikeCommandService.likePost(postId, memberId);

        // then
        assertThatThrownBy(() -> postLikeCommandService.likePost(postId, memberId))
                .isInstanceOf(DuplicatePostLikeException.class)
                .hasMessageContaining("이미 좋아요를 누른 게시글입니다.");
    }

    @Test
    @DisplayName("좋아요 후 좋아요 수가 정확히 1 증가한다")
    void 좋아요_후_카운트_증가() {
        // given
        Long postId = 200L;
        Long memberId = 300L;
        Long beforeCount = postLikeQueryRepository.countByPostId(postId);

        // when
        PostLikeResponse response = postLikeCommandService.likePost(postId, memberId);

        // then
        assertThat(response.likeCount()).isEqualTo(beforeCount + 1);
        assertThat(response.liked()).isTrue();
        assertThat(postLikeQueryRepository.countByPostId(postId)).isEqualTo(beforeCount + 1);
    }

    @Test
    @DisplayName("좋아요 취소 후 좋아요 수가 정확히 1 감소한다")
    void 좋아요_취소_후_카운트_감소() {
        // given
        Long postId = 300L;
        Long memberId = 400L;
        postLikeCommandService.likePost(postId, memberId);
        Long afterLikeCount = postLikeQueryRepository.countByPostId(postId);

        // when
        PostLikeResponse response = postLikeCommandService.unlikePost(postId, memberId);

        // then
        assertThat(response.likeCount()).isEqualTo(afterLikeCount - 1);
        assertThat(response.liked()).isFalse();
        assertThat(postLikeQueryRepository.countByPostId(postId)).isEqualTo(afterLikeCount - 1);
    }
}
