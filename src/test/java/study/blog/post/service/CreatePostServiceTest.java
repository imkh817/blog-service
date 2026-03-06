package study.blog.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.blog.post.application.PostCommandService;
import study.blog.post.domain.exception.*;
import study.blog.post.presentation.requset.CreatePostRequest;
import study.blog.post.presentation.response.PostSaveResponse;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.PostStatus;
import study.blog.post.infrastructure.persistence.command.PostCommandRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostCommandService 게시글 생성 테스트")
class CreatePostServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private PostCommandRepository postCommandRepository;

    private static final String THUMBNAIL_URL = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/thumbnail/test.jpg";

    @Test
    @DisplayName("게시글 생성 - 성공")
    void createPost_success() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL"),
                THUMBNAIL_URL
        );

        Post mockPost = Post.createPost(
                authorId,
                createPostRequest.title(),
                createPostRequest.content(),
                createPostRequest.postStatus(),
                createPostRequest.tagNames(),
                THUMBNAIL_URL,
                List.of()
        );

        given(postCommandRepository.save(any(Post.class))).willReturn(mockPost);

        // when
        PostSaveResponse response = postCommandService.createPost(authorId, createPostRequest);

        // then
        assertThat(response.title()).isEqualTo("테스트 제목");
        assertThat(response.content()).isEqualTo("테스트 본문");
        assertThat(response.postStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(response.viewCount()).isEqualTo(0L);
        assertThat(response.tags()).hasSize(3);
        assertThat(response.tags()).containsExactlyInAnyOrder("DDD", "Spring", "QueryDSL");
        assertThat(response.thumbnailUrl()).isEqualTo(THUMBNAIL_URL);
    }

    @Test
    @DisplayName("게시글 생성 - 제목이 비어있으면 안된다")
    void createPost_emptyTitle() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                null,
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL"),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(InValidPostTitleException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 본문이 비어있으면 안된다")
    void createPost_emptyContent() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "테스트 제목",
                null,
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL"),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(InValidPostContentException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 게시글 상태가 비어있으면 안된다")
    void createPost_emptyStatus() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "테스트 제목",
                "테스트 본문",
                null,
                List.of("DDD", "Spring", "QueryDSL"),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(InValidPostStatusException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 게시글 상태는 DRAFT 혹은 PUBLISHED 여야 한다")
    void createPost_invalidStatus() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "테스트 제목",
                "테스트 본문",
                PostStatus.HIDDEN,
                List.of("DDD", "Spring", "QueryDSL"),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(InValidPostStatusException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 태그가 비어있으면 안된다")
    void createPost_emptyTags() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of(),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(EmptyTagException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 중복 태그는 제거된다")
    void createPost_duplicateTags() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of("Java", "Spring", "Java", "Spring"),
                THUMBNAIL_URL
        );

        Post mockPost = Post.createPost(
                authorId,
                createPostRequest.title(),
                createPostRequest.content(),
                createPostRequest.postStatus(),
                createPostRequest.tagNames(),
                THUMBNAIL_URL,
                List.of()
        );

        given(postCommandRepository.save(any(Post.class))).willReturn(mockPost);

        // when
        PostSaveResponse response = postCommandService.createPost(authorId, createPostRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tags()).hasSize(2);
        assertThat(response.tags()).containsExactlyInAnyOrder("Java", "Spring");
        then(postCommandRepository).should(times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 - 태그는 10개를 초과할 수 없다")
    void createPost_moreThan10Tags() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of("Java", "Spring", "JPA", "QueryDSL",
                        "MyBatis", "Oracle", "MySQL",
                        "Redis", "RabbitMQ", "Kafka", "Elasticsearch"),
                THUMBNAIL_URL
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(TooManyTagsException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 썸네일 URL이 비어있으면 안된다")
    void createPost_emptyThumbnailUrl() {
        // given
        Long authorId = 1L;
        CreatePostRequest createPostRequest = new CreatePostRequest(
                "테스트 제목",
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("Java"),
                null
        );

        // when, then
        assertThatThrownBy(
                () -> postCommandService.createPost(authorId, createPostRequest)
        ).isInstanceOf(InValidThumbnailUrlException.class);
    }
}
