package study.blog.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.entity.Post;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostResponse createPost(Long authorId, CreatePostDto createPostDto){
        Post post = Post.createPost(
                authorId,
                createPostDto.title(),
                createPostDto.content(),
                createPostDto.postStatus(),
                createPostDto.tagNames()
        );
        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }


    @Transactional
    public PostResponse modifyPost(Long authorId, @Valid UpdatePostDto updatePostDto) {
        Post findPost = postRepository.findById(updatePostDto.postId())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다"));

        findPost.modifyPost(
                updatePostDto.title(),
                updatePostDto.content(),
                updatePostDto.tagNames()
        );

        return PostResponse.from(findPost);
    }

    @Transactional
    public PostResponse changeStatusToPublish(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.publish();
        return PostResponse.from(post);
    }

@Transactional
    public PostResponse changeStatusToHidden(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.hide();
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse changeStatusToDelete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        post.delete();
        return PostResponse.from(post);
    }
}
