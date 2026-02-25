package study.blog.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.comment.enums.CommentStatus;
import study.blog.comment.exception.InValidCommentContentException;
import study.blog.comment.exception.TooManyContentLengthException;
import study.blog.global.common.entity.BaseEntity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.util.StringUtils.hasText;

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

    private String authorNickName;

    @Column(length = 1000)
    private String content;

    private Long parentId;

    @Enumerated(value = STRING)
    private CommentStatus status;

    public static Comment createComment(Long postId, Long authorId, String content, Long parentId){
        validateContent(content);

        Comment comment = new Comment();
        comment.postId = postId;
        comment.authorId = authorId;
        comment.content = content;
        comment.parentId = parentId;
        comment.status = CommentStatus.ACTIVE;
        return comment;
    }

    private static void validateContent(String content){
        if(!hasText(content)){
            throw new InValidCommentContentException("댓글 내용은 필수 값입니다.");
        }

        if(content.length() > 1000){
            throw new TooManyContentLengthException("댓글 내용이 허용 글자를 초과하였습니다. 허용 글자: 1000, 현재 글자: " + content.length());
        }
    }



}
