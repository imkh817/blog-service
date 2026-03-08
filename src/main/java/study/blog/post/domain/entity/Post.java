package study.blog.post.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;
import study.blog.post.domain.PostStatus;
import study.blog.post.domain.exception.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.util.StringUtils.hasText;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "post", indexes = @Index(name = "idx_view_count", columnList = "view_count"))
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long authorId;

    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    private long viewCount;

    private long likeCount;

    private String thumbnailUrl;

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<PostTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    public static Post createPost(Long authorId, String title, String content, PostStatus postStatus, List<String> tagNames, String thumbnailUrl, List<String> imageUrls){
        validateTitle(title);
        validateContent(content);
        validatePostStatusForCreate(postStatus);
        validateTagNames(tagNames);
        validateThumbnailUrl(thumbnailUrl);

        Post post = new Post();
        post.authorId = authorId;
        post.title = title;
        post.content = content;
        post.postStatus = postStatus;
        post.thumbnailUrl = thumbnailUrl;
        post.viewCount = 0L;
        post.likeCount = 0L;
        post.addTags(tagNames);
        post.addPostImages(imageUrls);
        return post;
    }

    public static Post createDraft(Long authorId, String title, String content, List<String> tagNames, String thumbnailUrl) {
        validateTitle(title);

        Post post = new Post();
        post.authorId = authorId;
        post.title = title;
        post.content = content;
        post.postStatus = PostStatus.DRAFT;
        post.thumbnailUrl = thumbnailUrl;
        post.viewCount = 0L;
        post.likeCount = 0L;
        if (tagNames != null && !tagNames.isEmpty()) post.addTags(tagNames);
        return post;
    }

    public void updateDraft(String title, String content, List<String> tagNames, String thumbnailUrl) {
        validateModifiable();
        validateTitle(title);

        this.title = title;
        if (hasText(content)) this.content = content;
        if (hasText(thumbnailUrl)) this.thumbnailUrl = thumbnailUrl;
        if (tagNames != null && !tagNames.isEmpty()) modifyTags(tagNames);
        this.postStatus = PostStatus.DRAFT;
    }

    public void modifyPost(String title, String content, PostStatus postStatus, List<String> tags, String thumbnailUrl){
        validateModifiable();
        validateTitle(title);
        validateContent(content);
        validateTagNames(tags);
        validatePostStatusForModify(postStatus);

        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        if (hasText(thumbnailUrl)) this.thumbnailUrl = thumbnailUrl;
        modifyTags(tags);
    }

    private static void validatePostStatusForModify(PostStatus postStatus) {
        if (postStatus == null) {
            throw new InValidPostStatusException("게시글 상태는 필수입니다.");
        }
        if (postStatus == PostStatus.DELETED) {
            throw new InValidPostStatusException("수정 시 삭제 상태로 변경할 수 없습니다.");
        }
    }

    private void modifyTags(List<String> tags){
        this.tags.clear();
        addTags(tags);
    }

    private void addPostImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        imageUrls.forEach(url -> postImages.add(PostImage.createPostImage(url, this)));
    }

    private void addTags(List<String> tagNames){
        if(tagNames.isEmpty()) return;

        tagNames.stream()
                .distinct()
                .forEach(name -> tags.add(PostTag.createPostTag(name, this)));
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (!hasText(thumbnailUrl)) {
            throw new InValidThumbnailUrlException("대표 이미지는 필수입니다.");
        }
    }

    private static void validateTagNames(List<String> tags){
        if(tags.isEmpty()){
            throw new EmptyTagException("태그는 최소 1개 이상 등록해야 합니다.");
        }
        if(tags.size() > 10){
            throw new TooManyTagsException("태그는 최대 10개까지만 등록할 수 있습니다.");
        }
    }

    private static void validateTitle(String title){
        if(!hasText(title)){
            throw new InValidPostTitleException("제목은 필수입니다.");
        }

        if(title.length() > 100){
            throw new InValidPostTitleException("제목은 최대 100자까지 가능합니다");
        }
    }

    private static void validateContent(String content){
        if(!hasText(content)){
            throw new InValidPostContentException("본문은 필수입니다.");
        }
    }

    private static void validatePostStatusForCreate(PostStatus postStatus){
        if(postStatus == null){
            throw new InValidPostStatusException("게시글 상태는 필수입니다.");
        }

        if(!(postStatus == PostStatus.PUBLISHED || postStatus == PostStatus.DRAFT)){
            throw new InValidPostStatusException("게시글 상태는 임시 저장 또는 발행 상태여야 합니다.");
        }
    }

    private void validateModifiable(){
        if(this.postStatus == PostStatus.DELETED){
            throw new InValidPostStatusException("삭제된 게시글은 수정할 수 없습니다.");
        }
    }

    /**
     * 게시글이 PUBLISHED 상태일 때만 HIDE 가능
     */
    private void validatePostStatusToHide(PostStatus status){
        if(status.equals(PostStatus.HIDDEN)){
            throw new InValidPostStatusException("이미 숨김 처리 상태인 게시글입니다.");
        }

        if(!status.equals(PostStatus.PUBLISHED)){
            throw new InValidPostStatusException("게시글이 발행 상태인 경우에만 숨김 처리 가능합니다.");
        }
    }

    /**
     * 게시글이 HIDDEN 또는 DRAFT 상태일 때만 PUBLISH 가능
     */
    private void validatePostStatusToPublish(PostStatus status){
        if(PostStatus.DELETED.equals(status)){
            throw new InValidPostStatusException("삭제된 게시물은 다시 발행할 수 없습니다.");
        }
    }



    /**
     * 게시글이 DELETED 상태가 아니어야 DELETE 가능
     */
    private void validatePostStatusToDelete(PostStatus status){
        if(status.equals(PostStatus.DELETED)){
            throw new InValidPostStatusException("이미 삭제된 게시글입니다.");
        }
    }


    public void publish() {
        validatePostStatusToPublish(this.postStatus);
        this.postStatus = PostStatus.PUBLISHED;
    }

    public void hide() {
        validatePostStatusToHide(this.postStatus);
        this.postStatus = PostStatus.HIDDEN;
    }

    public void delete(){
        validatePostStatusToDelete(this.postStatus);
        this.postStatus = PostStatus.DELETED;
    }

}
