package study.blog.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.dto.*;
import study.blog.post.entity.Post;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.repository.command.PostCommandRepository;

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
        Post savedPost = commandRepository.save(post);
        return CreatePostResponse.from(savedPost);
    }

    /**
     * 게시글을 수정한다.
     *
     * tag 및 image 수정 필요
     */
    public ModifyPostResponse modifyPost(Long authorId, UpdatePostDto updatePostDto) {
        Post findPost = commandRepository.findById(updatePostDto.postId())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다"));

        findPost.modifyPost(
                updatePostDto.title(),
                updatePostDto.content(),
                updatePostDto.tagNames()
        );

        return ModifyPostResponse.from(findPost);
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
