package study.blog.post.service;

import jakarta.persistence.EntityManager;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
    void setUp(){
        Long authorId = 100L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목(setUp)",
                "테스트 본문(setUp)",
                PostStatus.DRAFT,
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
    void createPost_success2(){
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
    void modifyPost_Success(){
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
                PostStatus.PUBLISHED,
                List.of("Window", "Mac")
        );

        PostResponse modifiedPost = postService.modifyPost(authorId, updatePostDto);

        entityManager.flush();
        assertThat(modifiedPost.title()).isEqualTo("테스트 제목(수정)");
        assertThat(modifiedPost.content()).isEqualTo("테스트 본문(수정)");
        assertThat(modifiedPost.postStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(modifiedPost.tags()).containsExactlyInAnyOrder("Window", "Mac");
    }

    @Test
    @DisplayName("게시글의 상태를 발행으로 변경한다.")
    void changeStatusToPublish(){
        PostResponse response = postService.changeStatusToPublish(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.PUBLISHED);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글의 상태를 임시 저장으로 변경한다.")
    void changeStatusToDraft(){
        PostResponse response = postService.changeStatusToDraft(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.DRAFT);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글의 상태를 숨김으로 변경한다.")
    void changeStatusToHidden(){
        PostResponse response = postService.changeStatusToHidden(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.HIDDEN);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글의 상태를 삭제로 변경한다.")
    void changeStatusToDelete(){
        PostResponse response = postService.changeStatusToDelete(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(PostStatus.DELETED);
        entityManager.flush();
    }

}
