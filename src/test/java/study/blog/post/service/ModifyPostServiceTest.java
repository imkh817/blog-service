package study.blog.post.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;
import study.blog.post.exception.InValidPostStatusException;
import study.blog.post.repository.PostRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyPostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private EntityManager entityManager;

    private Post existingPost;

    private Long postId;

    private Long authorId;

    @BeforeEach
    void setUp() {
        postId = 1L;
        authorId = 100L;
        existingPost = Post.createPost(
                authorId,
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("Spring", "JPA", "Redis")
        );
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void modifyPost() {
        UpdatePostDto updatePostDto = new UpdatePostDto(
                postId,
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );

        when(postRepository.findById(any(Long.class))).thenReturn(Optional.of(existingPost));

        PostResponse modifiedPost = postService.modifyPost(authorId, updatePostDto);

        assertThat(modifiedPost.title()).isEqualTo("테스트 제목(수정)");
        assertThat(modifiedPost.content()).isEqualTo("테스트 본문(수정)");
        assertThat(modifiedPost.postStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(modifiedPost.tags()).containsExactlyInAnyOrder("Mac", "Window");

        assertThat(existingPost.getTitle()).isEqualTo("테스트 제목(수정)");
        assertThat(existingPost.getContent()).isEqualTo("테스트 본문(수정)");
    }

    @Test
    @DisplayName("삭제된 게시글은 수정하지 못한다.")
    void modifyDeletedPost() {
        UpdatePostDto updatePostDto = new UpdatePostDto(
                postId,
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );

        existingPost.delete();

        when(postRepository.findById(any(Long.class))).thenReturn(Optional.of(existingPost));

        assertThatThrownBy(() -> postService.modifyPost(authorId, updatePostDto))
                .isInstanceOf(InValidPostStatusException.class)
                .hasMessageContaining("삭제된 게시글은 수정할 수 없습니다.");
    }
}