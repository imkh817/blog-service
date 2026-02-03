package study.blog.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.entity.Post;
import study.blog.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
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


}
