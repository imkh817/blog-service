package study.blog.like.postlike.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import study.blog.global.IntegrationTestSupport;
import study.blog.like.postlike.dto.PostLikeResponse;
import study.blog.like.postlike.entity.PostLike;
import study.blog.like.postlike.repository.PostLikeRepository;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class PostLikeIntegrationTest extends IntegrationTestSupport {

    @Autowired private PostLikeService postLikeService;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private MemberRepository memberRepository;

    @Test
    @DisplayName("100개의 동시 요청이 들어와도 게시글 좋아요가 정확히 처리되는지 검증하는 통합 테스트")
    void 게시글_좋아요_통합_테스트() throws InterruptedException {
        // given
        Long postId = 1L;
        List<Long> memberIds = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Member member = memberRepository.save(
                    Member.createMember("test" + i + "@naver.com", "password" + i, "nickname" + i)
            );
            memberIds.add(member.getId());
        }

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    PostLikeResponse postLikeResponse = postLikeService.likePost(postId, memberId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // then
        Long count = postLikeRepository.countByPostId(postId);

        assertThat(count).isEqualTo(100L);
    }
}