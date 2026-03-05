package study.blog.post.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.repository.query.CommentReader;
import study.blog.like.postlike.repository.query.PostLikeReader;
import study.blog.member.repository.MemberReader;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;
import study.blog.post.event.PostViewTracker;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.infra.ViewCountReader;
import study.blog.post.repository.query.PostQueryRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostQueryRepository queryRepository;
    private final PostViewTracker viewTracker;
    private final PostLikeReader postLikeReader;
    private final CommentReader commentReader;
    private final MemberReader memberReader;
    private final ViewCountReader viewCountReader;

    /**
     * 게시글 목록 조회
     *
     * 목록 조회시 조회수는 post.viewCount(DB 값) 기준
     * Redis의 미플러시 조회수는 반영되지 않으며, 최대 5분(스케쥴러 주기) 지연 허용
     *
     * @param memberId - 사용자 ID / 비회원인 경우에는 Null
     * @param condition - 검색 조건
     * @param pageable - 페이징 조건
     */
    public Page<PostResponse> searchPostByCondition(Long memberId, PostSearchCondition condition, Pageable pageable) {
        List<Post> posts = queryRepository.searchPostsByCondition(condition, pageable);
        long total = queryRepository.countPostByCondition(condition);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();

        Set<Long> likedPostIds = postLikeReader.findLikedPostIds(memberId, postIds);
        Map<Long, Long> commentCounts = commentReader.getCommentCounts(postIds);
        Map<Long, String> nicknameMap = memberReader.getNicknames(authorIds);

        List<PostResponse> content = posts.stream()
                .map(post -> PostResponse.from(
                                post,
                                nicknameMap.getOrDefault(post.getAuthorId(), "익명"),
                                likedPostIds.contains(post.getId()),
                                commentCounts.getOrDefault(post.getId(), 0L)
                        )
                )
                .toList();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * MyPage 게시글 목록 조회
     * @param memberId
     * @param condition
     * @param pageable
     */
    public Page<PostResponse> findPostsByAuthorId(Long memberId, PostSearchCondition condition, Pageable pageable) {
        List<Post> posts = queryRepository.searchMemberPosts(memberId, condition, pageable);
        long total = queryRepository.countMemberPosts(memberId, condition);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();

        Set<Long> likedPostIds = postLikeReader.findLikedPostIds(memberId, postIds);
        Map<Long, Long> commentCounts = commentReader.getCommentCounts(postIds);
        Map<Long, String> nicknameMap = memberReader.getNicknames(authorIds);

        List<PostResponse> content = posts.stream()
                .map(post -> PostResponse.from(
                        post,
                        nicknameMap.getOrDefault(post.getAuthorId(), "익명"),
                        likedPostIds.contains(post.getId()),
                        commentCounts.getOrDefault(post.getId(), 0L)))
                .toList();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 게시글 단건을 조회한다.
     *
     * 조회수 계산
     * - 조회수 = DB에 플러시된 값 + Redis에 누적된 미플러시 값
     * - Redis 키가 없거나(Redis flush 직후), Redis 장애 시 Redis 누적분은 0으로 간주한다.
     *
     * 부가 동작
     * - Query(조회) 서비스이지만, 조회 이벤트를 발행하여 조회수 증가 처리를 트리거한다.
     *   (중복 조회 방지 정책 및 실제 카운트 반영은 ViewTracker 내부에서 처리된다.)
     *
     * @param postId   게시글 ID
     * @param memberId 사용자 ID (비회원인 경우 null)
     * @param request  조회수 정책에 사용하는 식별 정보(IP 등)를 추출하기 위한 요청 객체
     */
    public PostResponse findPost(Long postId, Long memberId, HttpServletRequest request) {
        Post post = queryRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        boolean isLikedByMe = memberId != null && postLikeReader.hasLiked(memberId, postId);
        long viewCount = viewCountReader.getViewCount(postId) + post.getViewCount();
        long commentCount = commentReader.getCommentCount(postId);
        String authorNickname = memberReader.getAuthorNickName(post.getAuthorId());

        viewTracker.track(postId, memberId, request);
        return PostResponse.from(post, viewCount, authorNickname, isLikedByMe, commentCount);
    }
}
