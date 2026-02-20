package study.blog.like.postlike.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_member",
                        columnNames = {"post_id", "member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostLike extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long postId;

    private Long memberId;

    public static PostLike createPostLike(Long postId, Long memberId){
        PostLike postLike = new PostLike();
        postLike.postId = postId;
        postLike.memberId = memberId;
        return postLike;
    }


}
