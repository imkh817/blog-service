package study.blog.post.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "post_image", indexes = @Index(name = "idx_post_image_post_id", columnList = "post_id"))
public class PostImage {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public static PostImage createPostImage(String imageUrl, Post post) {
        PostImage postImage = new PostImage();
        postImage.imageUrl = imageUrl;
        postImage.post = post;
        return postImage;
    }
}
