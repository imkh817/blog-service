package study.blog.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.blog.post.application.PostCommandService;
import study.blog.post.presentation.response.PostSaveResponse;
import study.blog.post.presentation.requset.UpdatePostRequest;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.PostStatus;
import study.blog.post.domain.exception.InValidPostStatusException;
import study.blog.post.domain.exception.PostNotFoundException;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostCommandService 게시글 수정 테스트")
class ModifyPostServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private PostCommandRepository postCommandRepository;

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
                List.of("Spring", "JPA", "Redis"),
                "https://test-thumbnail.jpg",
                List.of()
        );
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void modifyPost_success() {
        // given
        UpdatePostRequest updatePostRequest = new UpdatePostRequest(
                postId,
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );

        when(postCommandRepository.findById(any(Long.class))).thenReturn(Optional.of(existingPost));

        // when
        PostSaveResponse response = postCommandService.modifyPost(authorId, updatePostRequest);

        // then
        assertThat(response.title()).isEqualTo("테스트 제목(수정)");
        assertThat(response.content()).isEqualTo("테스트 본문(수정)");
        assertThat(response.postStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(response.tags()).containsExactlyInAnyOrder("Mac", "Window");

        assertThat(existingPost.getTitle()).isEqualTo("테스트 제목(수정)");
        assertThat(existingPost.getContent()).isEqualTo("테스트 본문(수정)");
    }

    @Test
    @DisplayName("삭제된 게시글은 수정하지 못한다")
    void modifyPost_deletedPost() {
        // given
        UpdatePostRequest updatePostRequest = new UpdatePostRequest(
                postId,
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );

        existingPost.delete();
        when(postCommandRepository.findById(any(Long.class))).thenReturn(Optional.of(existingPost));

        // when, then
        assertThatThrownBy(() -> postCommandService.modifyPost(authorId, updatePostRequest))
                .isInstanceOf(InValidPostStatusException.class)
                .hasMessageContaining("삭제된 게시글은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게시글은 수정하지 못한다")
    void modifyPost_notFound() {
        // given
        UpdatePostRequest updatePostRequest = new UpdatePostRequest(
                postId,
                "테스트 제목(수정)",
                "테스트 본문(수정)",
                List.of("Mac", "Window")
        );

        when(postCommandRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> postCommandService.modifyPost(authorId, updatePostRequest))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 발행 - DRAFT 상태에서 성공")
    void changeStatusToPublish_fromDraft() {
        // given
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when
        postCommandService.changeStatusToPublish(postId);

        // then
        assertThat(existingPost.getPostStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("게시글 발행 - 이미 발행된 게시글은 다시 발행할 수 없다")
    void changeStatusToPublish_alreadyPublished() {
        // given
        existingPost.publish();
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when, then
        assertThatThrownBy(() -> postCommandService.changeStatusToPublish(postId))
                .isInstanceOf(InValidPostStatusException.class);
    }

    @Test
    @DisplayName("게시글 숨김 - PUBLISHED 상태에서 성공")
    void changeStatusToHidden_fromPublished() {
        // given
        existingPost.publish();
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when
        postCommandService.changeStatusToHidden(postId);

        // then
        assertThat(existingPost.getPostStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("게시글 숨김 - PUBLISHED 상태가 아니면 숨김 처리할 수 없다")
    void changeStatusToHidden_notPublished() {
        // given - existingPost is DRAFT
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when, then
        assertThatThrownBy(() -> postCommandService.changeStatusToHidden(postId))
                .isInstanceOf(InValidPostStatusException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void changeStatusToDelete_success() {
        // given
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when
        postCommandService.changeStatusToDelete(postId);

        // then
        assertThat(existingPost.getPostStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("게시글 삭제 - 이미 삭제된 게시글은 다시 삭제할 수 없다")
    void changeStatusToDelete_alreadyDeleted() {
        // given
        existingPost.delete();
        when(postCommandRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when, then
        assertThatThrownBy(() -> postCommandService.changeStatusToDelete(postId))
                .isInstanceOf(InValidPostStatusException.class);
    }
}
