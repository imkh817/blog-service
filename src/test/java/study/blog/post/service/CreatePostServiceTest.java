package study.blog.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 유스케이스 테스트")
class CreatePostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

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

        Post mockPost = Post.createPost(
                authorId,
                createPostDto.title(),
                createPostDto.content(),
                createPostDto.postStatus(),
                createPostDto.tagNames()
        );

        given(postRepository.save(any(Post.class))).willReturn(mockPost);

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
    @DisplayName("게시글 생성 - 제목이 비어있으면 안된다.")
    void createPost_WithEmptyTitle(){
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                null,
                "테스트 본문",
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(InValidPostTitleException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 본문이 비어있으면 안된다.")
    void createPost_WithEmptyContent(){
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                null,
                PostStatus.DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(InValidPostContentException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 게시글 상태가 비어있으면 안된다.")
    void createPost_WithEmptyStatus(){
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                "테스트 본문",
                null,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(InValidPostStatusException.class);
    }

    @Test
    @DisplayName("게시글 생성 - 게시글 상태가 DRAFT 혹은 PUBLISH 여야 한다.")
    void createPost_WithInvalidStatus(){
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "테스트 제목",
                "테스트 본문",
                PostStatus.HIDDEN,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(InValidPostStatusException.class);
    }



    @Test
    @DisplayName("게시글 생성 - 태그가 비어있으면 안된다.")
    void createPost_WithEmptyTags() {
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of()
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(EmptyTagException.class);

    }

    @Test
    @DisplayName("게시글 생성 - 중복 태그는 제거된다.")
    void createPost_WithDuplicateTags() {
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of("Java", "Spring", "Java", "Spring")
        );

        Post mockPost = Post.createPost(
                authorId,
                createPostDto.title(),
                createPostDto.content(),
                createPostDto.postStatus(),
                createPostDto.tagNames()
        );

        given(postRepository.save(any(Post.class))).willReturn(mockPost);

        // when
        PostResponse response = postService.createPost(authorId, createPostDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tags()).hasSize(2);  // 중복 제거됨
        assertThat(response.tags()).containsExactlyInAnyOrder("Java", "Spring");

        then(postRepository).should(times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 - 태그는 10개를 초과할 수 없다.")
    void createPost_moreThan10Tags() {
        // given
        Long authorId = 1L;
        CreatePostDto createPostDto = new CreatePostDto(
                "제목",
                "본문",
                PostStatus.PUBLISHED,
                List.of("Java", "Spring", "Java", "Spring"
                        ,"QueryDSL", "MyBatis", "Oracle", "MySQL"
                        , "JPA", "Redis", "RabbitMQ")
        );

        // when, then
        assertThatThrownBy(
                () -> postService.createPost(authorId, createPostDto)
        ).isInstanceOf(TooManyTagsException.class);
    }
}
