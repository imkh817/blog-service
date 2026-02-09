package study.blog.post.service;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import study.blog.global.IntegrationTestSupport;
import study.blog.post.dto.CreatePostDto;
import study.blog.post.dto.PostResponse;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.dto.UpdatePostDto;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;
import study.blog.post.exception.*;
import study.blog.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static study.blog.post.enums.PostStatus.*;
import static study.blog.post.enums.PostStatus.PUBLISHED;

class PostServiceIntegrationTest extends IntegrationTestSupport {

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
        for(int i=1; i<= 10; i++){
            PostStatus postStatus = i%2 == 0 ? PUBLISHED : DRAFT;
            CreatePostDto createPostDto = new CreatePostDto(
                    "테스트 제목(setUp)" + i,
                    "테스트 본문(setUp)" + i,
                    postStatus,
                    List.of("Note"+i, "Computer"+i, "Watch"+i)
            );

            Post post = Post.createPost(
                    authorId,
                    createPostDto.title(),
                    createPostDto.content(),
                    createPostDto.postStatus(),
                    createPostDto.tagNames()
            );

            savedPost = postRepository.save(post);

        }

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
                DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        // when
        PostResponse response = postService.createPost(authorId, createPostDto);

        //then
        assertThat(response.title()).isEqualTo("테스트 제목");
        assertThat(response.content()).isEqualTo("테스트 본문");
        assertThat(response.postStatus()).isEqualTo(DRAFT);
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
                DRAFT,
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
                DRAFT,
                List.of("DDD", "Spring", "QueryDSL")
        );

        PostResponse savedPost = postService.createPost(authorId, createPostDto);

        PostResponse response = postService.changeStatusToPublish(savedPost.postId());
        assertThat(response.postStatus()).isEqualTo(PUBLISHED);
        entityManager.flush();
    }


    @Test
    @DisplayName("게시글의 상태를 숨김으로 변경한다.")
    void changeStatusToHidden() {
        PostResponse response = postService.changeStatusToHidden(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(HIDDEN);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글의 상태를 삭제로 변경한다.")
    void changeStatusToDelete() {
        PostResponse response = postService.changeStatusToDelete(savedPost.getId());
        assertThat(response.postStatus()).isEqualTo(DELETED);
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
        assertThat(post.postStatus()).isEqualTo(PUBLISHED);
        assertThat(post.tags()).containsExactlyInAnyOrder("Note", "Computer", "Watch");
    }

    @Test
    @DisplayName("검색 조건을 넣어 게시글을 조회한다.")
    void searchPostByCondition(){
        PageRequest pageRequest = PageRequest.of(0, 10);
        PostSearchCondition condition = new PostSearchCondition(
                "본문",
                //List.of("Spring", "DDD", "Domain")
                null,
                List.of(HIDDEN, PUBLISHED),
                null,
                null
        );

        List<PostResponse> postResponses = postService.searchPostByCondition(condition, pageRequest);
        for(PostResponse response : postResponses){
            System.out.println("response = " + response);
        }
        assertThat(postResponses.size()).isEqualTo(4);
    }

}
