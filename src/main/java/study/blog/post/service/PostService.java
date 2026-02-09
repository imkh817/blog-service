package study.blog.post.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;
import study.blog.post.exception.PostNotFoundException;
import study.blog.post.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final EntityManager em;

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

    public PostResponse findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        return PostResponse.from(post);
    }

    public List<PostResponse> searchPostByCondition(PostSearchCondition condition, Pageable pageable) {
        List<Post> posts = postRepository.searchPostByCondition(condition, pageable);
        return posts.stream()
                .map(PostResponse::from)
                .toList();
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
                    List.of("Spring" + i, "QueryDSL" + i, "Domain" + i)
            );
            em.persist(post);

            if (i % 1000 == 0) {
                em.flush();
                em.clear();
            }
        }
    }
}
