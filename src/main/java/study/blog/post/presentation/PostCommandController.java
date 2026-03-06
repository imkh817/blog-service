package study.blog.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.post.application.PostCommandService;
import study.blog.post.presentation.requset.CreatePostRequest;
import study.blog.post.presentation.requset.UpdatePostRequest;
import study.blog.post.presentation.response.PostSaveResponse;
import study.blog.post.presentation.response.PostStatusUpdateResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostCommandController {
    private final PostCommandService commandService;

    /**
     * 게시글을 생성한다.
     *
     * @param memberId      로그인 사용자 ID
     * @param createPostRequest 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostSaveResponse> createPost(@LoginMember Long memberId,
                                                    @RequestBody @Valid CreatePostRequest createPostRequest) {
        PostSaveResponse post = commandService.createPost(memberId, createPostRequest);
        return ApiResponse.success(post);
    }

    /**
     * 게시글을 수정한다.
     *
     * @param memberId       로그인 사용자 ID
     * @param updatePostRequest  게시글 수정 요청 데이터
     * @return 수정된 게시글 정보
     */
    @PutMapping
    public ApiResponse<PostSaveResponse> modifyPost(@LoginMember Long memberId,
                                                    @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        PostSaveResponse response = commandService.modifyPost(memberId, updatePostRequest);
        return ApiResponse.success(response);
    }

    /**
     * 게시글 상태를 발행(PUBLISH)으로 변경한다.
     *
     * @param postId 게시글 ID
     * @return 변경된 게시글 상태 정보
     */
    @PostMapping("/{postId}/publish")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToPublish(@PathVariable Long postId){
        PostStatusUpdateResponse response = commandService.changeStatusToPublish(postId);
        return ApiResponse.success(response);
    }

    /**
     * 게시글 상태를 숨김(HIDDEN)으로 변경한다.
     *
     * @param postId 게시글 ID
     * @return 변경된 게시글 상태 정보
     */
    @PostMapping("/{postId}/hide")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToHidden(@PathVariable Long postId){
        PostStatusUpdateResponse response = commandService.changeStatusToHidden(postId);
        return ApiResponse.success(response);
    }

    /**
     * 게시글 상태를 삭제(DELETE)로 변경한다.
     *
     * @param postId 게시글 ID
     * @return 변경된 게시글 상태 정보
     */
    @PostMapping("/{postId}/delete")
    public ApiResponse<PostStatusUpdateResponse> changeStatusToDelete(@PathVariable Long postId){
        PostStatusUpdateResponse response = commandService.changeStatusToDelete(postId);
        return ApiResponse.success(response);
    }


}
