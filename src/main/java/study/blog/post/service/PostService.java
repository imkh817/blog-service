package study.blog.post.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.comment.repository.CommentQueryRepository;
import study.blog.like.postlike.repository.PostLikedChecker;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;
import study.blog.post.dto.*;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;
import study.blog.post.event.PostViewedEvent;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.infra.ViewCountReader;
import study.blog.post.repository.PostRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final EntityManager em;
    private final PostLikedChecker postLikedChecker;
    private final ViewCountReader viewCountReader;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentQueryRepository commentQueryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreatePostResponse createPost(Long authorId, CreatePostDto createPostDto) {
        Post post = Post.createPost(
                authorId,
                createPostDto.title(),
                createPostDto.content(),
                createPostDto.postStatus(),
                createPostDto.tagNames(),
                createPostDto.thumbnailUrl(),
                List.of()
        );
        Post savedPost = postRepository.save(post);
        return CreatePostResponse.from(savedPost);
    }


    @Transactional
    public ModifyPostResponse modifyPost(Long authorId, @Valid UpdatePostDto updatePostDto) {
        Post findPost = postRepository.findById(updatePostDto.postId())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다"));

        findPost.modifyPost(
                updatePostDto.title(),
                updatePostDto.content(),
                updatePostDto.tagNames()
        );

        return ModifyPostResponse.from(findPost);
    }

    @Transactional
    public PostStatusUpdateResponse changeStatusToPublish(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.publish();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }

    @Transactional
    public PostStatusUpdateResponse changeStatusToHidden(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.hide();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }

    @Transactional
    public PostStatusUpdateResponse changeStatusToDelete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.delete();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }

    /**
     * 게시글 단건 조회
     *
     * 조회수 = DB에 플러시된 값 + Redis에 아직 남아있는 미플러시 값의 합산
     * Redis 키가 없는 경우(스케쥴러 플러시 직후) or Redis 연결 오류 시 0으로 처리
     *
     * @param postId - 게시글 ID
     * @param memberId - 사용자 ID
     * @param request - Request (조회수 증가 정책에 사용하는 IP를 추출할 용도)
     */
    public PostResponse findPost(Long postId, Long memberId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        boolean isLikedByMe = memberId != null && postLikedChecker.existsByMemberIdAndPostId(memberId, postId);

        publishViewEvent(postId,memberId,request);
        long viewCount = viewCountReader.getViewCount(postId) + post.getViewCount();
        long commentCount = commentQueryRepository.countCommentByPostId(postId);
        String authorNickname = memberRepository.findById(post.getAuthorId())
                .map(Member::getNickname)
                .orElse("알 수 없음");
        return PostResponse.from(post, viewCount, authorNickname, isLikedByMe, commentCount);
    }

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
        List<Post> posts = postRepository.searchPostsByCondition(memberId, condition, pageable);
        long total = postRepository.countPostByCondition(condition);

        Set<Long> likedPostIds = getLikedPostIds(memberId, posts);
        Map<Long, Long> commentCounts = getCommentCount(posts);
        Map<Long, String> nicknameMap = getNicknameMap(posts);

        List<PostResponse> content = posts.stream()
                .map(post -> PostResponse.from(
                        post,
                        nicknameMap.getOrDefault(post.getAuthorId(), "알 수 없음"),
                        likedPostIds.contains(post.getId()),
                        commentCounts.getOrDefault(post.getId(), 0L)
                        )
                )
                .toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Transactional
    public void bulkCreatePosts() {

        Long authorId = 1L;

        for (int i = 0; i < 100000; i++) {
            Post post = Post.createPost(
                    authorId,
                    "제목 " + i,
                    "본문 " + i,
                    PostStatus.DRAFT,
                    List.of("Spring" + i, "QueryDSL" + i, "Domain" + i),
                    "https://placeholder-thumbnail.jpg",
                    List.of()
            );
            em.persist(post);

            if (i % 1000 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    private String extractIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }

    /**
     * 게시글 조회 이벤트 발행
     *
     * 조회 요청 시 조회수 증가 로직을 동기적으로 처리하지 않고
     * 이벤트로 발행하여 비동기 처리하도록 한다.
     * 게시글 조회 응답 시간을 최소화하고, 조회수 증가 정책(IP, 사용자 중복 체크 등)은 이벤트 리스너에서 처리한다.
     */
    private void publishViewEvent(Long postId, Long memberId, HttpServletRequest request){
        eventPublisher.publishEvent(new PostViewedEvent(postId, memberId, extractIp(request)));
    }

    /**
     * MyPage 게시글 목록 조회
     * @param memberId
     * @param condition
     * @param pageable
     */
    public Page<PostResponse> findPostsByAuthorId(Long memberId, PostSearchCondition condition, Pageable pageable) {
        List<Post> posts = postRepository.searchMemberPosts(memberId, condition, pageable);
        long total = postRepository.countMemberPosts(memberId, condition);

        Set<Long> likedPostIds = getLikedPostIds(memberId, posts);
        Map<Long, Long> commentCounts = getCommentCount(posts);
        Map<Long, String> nicknameMap = getNicknameMap(posts);

        List<PostResponse> content = posts.stream()
                .map(post -> PostResponse.from(
                                post,
                                nicknameMap.getOrDefault(post.getAuthorId(), "알 수 없음"),
                                likedPostIds.contains(post.getId()),
                                commentCounts.getOrDefault(post.getId(), 0L)
                        )
                )
                .toList();
        return new PageImpl<>(content, pageable, total);
    }

    private Set<Long> getLikedPostIds(Long memberId, List<Post> posts){
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return (memberId == null)
                ? Set.of()
                : new HashSet<>(postLikedChecker.findPostIdByMemberIdAndPostIdIn(memberId, postIds));
    }

    private Map<Long, Long> getCommentCount(List<Post> posts){
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return commentQueryRepository.countCommentByPostIds(postIds);
    }

    private Map<Long, String> getNicknameMap(List<Post> posts) {
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        return memberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));
    }
}
