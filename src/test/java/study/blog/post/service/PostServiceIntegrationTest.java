package study.blog.post.service;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;
import study.blog.post.exception.*;
import study.blog.post.repository.PostRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    private Post savedPost;

    @BeforeEach
    void setUp() {
        Long authorId = 100L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목(setUp)",
                "테스트 본문(setUp)",
                PostStatus.PUBLISHED,
                List.of("Note", "Computer", "Watch")
        );

        Post post = Post.createPost(
                authorId,
                createPostDto.title(),
                createPostDto.content(),
                createPostDto.postStatus(),
                createPostDto.tagNames()
        );

        savedPost = postRepository.save(post);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("게시글 생성 - 성공")
    void createPost() {
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when
        PostResponse response = postService.createPost(authorId, createPostDto);

        //then
        assertThat(response.title()).isEqualTo("테스트 제목");
        assertThat(response.content()).isEqualTo("테스트 본문");
        assertThat(response.postStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(response.viewCount()).isEqualTo(0L);
        assertThat(response.tags()).hasSize(3);
        assertThat(response.tags()).containsExactlyInAnyOrder("DDD", "Spring", "QueryDSL");
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void modifyPost() {
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        PostResponse savedPost = postService.createPost(authorId, createPostDto);


        entityManager.flush();
        entityManager.clear();
        UpdatePostDto updatePostDto = new UpdatePostDto(
                savedPost.postId(),
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Window", "Mac")
        );

        PostResponse modifiedPost = postService.modifyPost(authorId, updatePostDto);

        entityManager.flush();
        assertThat(modifiedPost.title()).isEqualTo("테스트 제목(수정)");
        assertThat(modifiedPost.content()).isEqualTo("테스트 본문(수정)");
        assertThat(modifiedPost.tags()).containsExactlyInAnyOrder("Window", "Mac");
    }

    @Test
    @DisplayName("게시글의 상태를 발행으로 변경한다.")
    void changeStatusToPublish() {
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        PostResponse savedPost = postService.createPost(authorId, createPostDto);

        PostResponse response = postService.changeStatusToPublish(savedPost.postId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.PUBLISHED);
        entityManager.flush();
    }


    @Test
    @DisplayName("게시글의 상태를 숨김으로 변경한다.")
    void changeStatusToHidden() {
        PostResponse response = postService.changeStatusToHidden(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.HIDDEN);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글의 상태를 삭제로 변경한다.")
    void changeStatusToDelete() {
        PostResponse response = postService.changeStatusToDelete(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.DELETED);
        entityManager.flush();
    }

    @Test
    @DisplayName("삭제된 게시글은 수정하지 못한다.")
    void modifyDeletedPost() {
        PostResponse postResponse = postService.changeStatusToDelete(savedPost.getId());
        entityManager.flush();

        UpdatePostDto updatePostDto = new UpdatePostDto(
                savedPost.getId(),
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );
        assertThatThrownBy(() -> postService.modifyPost(savedPost.getAuthorId(), updatePostDto)
        ).isInstanceOf(InValidPostStatusException.class)
                .hasMessageContaining("삭제된 게시글은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("게시글(단건)을 조회한다.")
    void findPost(){
        PostResponse post = postService.findPost(savedPost.getId());

        assertThat(post.title()).isEqualTo("테스트 제목(setUp)");
        assertThat(post.content()).isEqualTo("테스트 본문(setUp)");
        assertThat(post.postStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.tags()).containsExactlyInAnyOrder("Note", "Computer", "Watch");
    }

}
