package study.blog.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostTag {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public static PostTag createPostTag(String name, Post post){
        PostTag postTag = new PostTag();
        postTag.name = name;
        postTag.post = post;
        return postTag;
    }
}
