package study.blog.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.comment.enums.CommentStatus;
import study.blog.comment.entity.global.entity.BaseEntity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    private Long authorId;

    @Column(length = 1000)
    private String content;

    private Long parentId;

    @Enumerated(value = STRING)
    private CommentStatus status;



}
