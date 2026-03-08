package study.blog.post.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.exception.PostNotFoundException;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;
import study.blog.post.presentation.requset.CreatePostRequest;
import study.blog.post.presentation.requset.SaveDraftRequest;
import study.blog.post.presentation.requset.UpdatePostRequest;
import study.blog.post.presentation.response.PostSaveResponse;
import study.blog.post.presentation.response.PostStatusUpdateResponse;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService {
    private final PostCommandRepository commandRepository;

    /**
     * 게시글을 생성한다.
     *
     * 게시글 생성 시 tag와 image도 같이 생성된다.
     */
    public PostSaveResponse createPost(Long authorId, CreatePostRequest createPostRequest) {
        Post post = Post.createPost(
                authorId,
                createPostRequest.title(),
                createPostRequest.content(),
                createPostRequest.postStatus(),
                createPostRequest.tagNames(),
                createPostRequest.thumbnailUrl(),
                List.of()
        );
        Post savedPost = commandRepository.save(post);
        return PostSaveResponse.from(savedPost);
    }

    /**
     * 게시글을 임시저장한다.
     *
     * postId가 없으면 신규 생성, 있으면 기존 임시저장 게시글을 수정한다.
     * 제목만 필수이며 내용, 태그, 썸네일은 선택이다.
     */
    public PostSaveResponse saveDraft(Long authorId, SaveDraftRequest request) {
        if (request.postId() == null) {
            Post post = Post.createDraft(authorId, request.title(), request.content(), request.tagNames(), request.thumbnailUrl());
            return PostSaveResponse.from(commandRepository.save(post));
        }

        Post post = commandRepository.findById(request.postId())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.updateDraft(request.title(), request.content(), request.tagNames(), request.thumbnailUrl());
        return PostSaveResponse.from(post);
    }

    /**
     * 게시글을 수정한다.
     *
     * tag 및 image 수정 필요
     */
    public PostSaveResponse modifyPost(Long authorId, UpdatePostRequest updatePostRequest) {
        Post findPost = commandRepository.findById(updatePostRequest.postId())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다"));

        findPost.modifyPost(
                updatePostRequest.title(),
                updatePostRequest.content(),
                updatePostRequest.postStatus(),
                updatePostRequest.tagNames(),
                updatePostRequest.thumbnailUrl()
        );

        return PostSaveResponse.from(findPost);
    }

    /**
     * 게시글 상태를 발행(PUBLISH)으로 변경한다.
     *
     * 비즈니스 규칙
     * - 이미 발행된 게시글은 다시 발행할 수 없다.
     * - 삭제된 게시글은 발행할 수 없다.
     */
    public PostStatusUpdateResponse changeStatusToPublish(Long postId) {
        Post post = commandRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.publish();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }

    /**
     * 게시글 상태를 숨김(HIDDEN)으로 변경한다.
     *
     * 비즈니스 규칙
     * - 발행 상태인 게시글만 숨김 상태로 변경할 수 있다.
     */
    public PostStatusUpdateResponse changeStatusToHidden(Long postId) {
        Post post = commandRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.hide();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }

    /**
     * 게시글 상태를 삭제(DELETE)로 변경한다.
     *
     * 비즈니스 규칙
     * - 이미 삭제된 게시글은 다시 삭제할 수 없다.
     */
    public PostStatusUpdateResponse changeStatusToDelete(Long postId) {
        Post post = commandRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.delete();
        return PostStatusUpdateResponse.from(postId, post.getPostStatus());
    }
}
