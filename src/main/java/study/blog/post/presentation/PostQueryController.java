package study.blog.post.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.post.application.PostQueryService;
import study.blog.post.presentation.requset.PostSearchCondition;
import study.blog.post.presentation.response.PostDetailResponse;
import study.blog.post.presentation.response.PostSummaryResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostQueryController {

    private final PostQueryService queryService;

    /**
     * 게시글 목록을 조회한다.
     *
     * 페이징 정보 중 전체 개수는 응답 헤더(X-Total-Count)에 담아
     * 프론트엔드에서 페이지 계산에 사용할 수 있도록 한다.
     *
     * @param condition 게시글 검색 조건
     * @param pageable  페이징 정보
     * @param response  전체 게시글 수를 전달하기 위한 HTTP 응답 객체
     */
    @GetMapping("/search")
    public ApiResponse<List<PostSummaryResponse>> searchPostByCondition(@ModelAttribute PostSearchCondition condition,
                                                                        @PageableDefault Pageable pageable,
                                                                        HttpServletResponse response) {
        Page<PostSummaryResponse> page = queryService.searchPostByCondition(condition, pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    @GetMapping
    public ApiResponse<List<PostSummaryResponse>> getMainPostsBySort(@PageableDefault Pageable pageable,
                                                                     HttpServletResponse response) {
        Page<PostSummaryResponse> page = queryService.getMainPostsBySort(pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    /**
     * 로그인 사용자가 작성한 게시글 목록을 조회한다.
     *
     * 페이징 정보 중 전체 게시글 수는 응답 헤더(X-Total-Count)에 담아
     * 프론트엔드에서 페이지네이션 계산에 사용할 수 있도록 한다.
     *
     * @param memberId  로그인 사용자 ID
     * @param condition 게시글 검색 조건
     * @param pageable  페이징 정보
     * @param response  전체 게시글 수를 전달하기 위한 HTTP 응답 객체
     */
    @GetMapping("/my")
    public ApiResponse<List<PostSummaryResponse>> findPostsByAuthorId(@LoginMember Long memberId,
                                                                      @ModelAttribute PostSearchCondition condition,
                                                                      @PageableDefault Pageable pageable,
                                                                      HttpServletResponse response) {
        Page<PostSummaryResponse> page = queryService.findPostsByAuthorId(memberId, condition, pageable);
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ApiResponse.success(page.getContent());
    }

    /**
     * 게시글을 단건 조회한다.
     *
     * 조회 시 조회수 증가 정책이 적용되며,
     * 동일 사용자/IP의 중복 조회는 일정 시간(10분) 동안 제한한다.
     *
     * @param postId   게시글 ID
     * @param memberId 로그인 사용자 ID (비로그인 시 null)
     * @param request  조회수 정책에 사용하는 사용자 식별 정보(IP 등)를 추출하기 위한 요청 객체
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> findPost(@PathVariable Long postId,
                                                    @LoginMember(required = false) Long memberId,
                                                    HttpServletRequest request) {
        PostDetailResponse post = queryService.findPost(postId, memberId, request);
        return ApiResponse.success(post);
    }

}
