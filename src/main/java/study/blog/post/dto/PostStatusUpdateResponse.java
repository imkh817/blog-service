package study.blog.post.dto;

import study.blog.post.enums.PostStatus;

public record PostStatusUpdateResponse(
        Long postId,
        PostStatus postStatus
) {

    public static PostStatusUpdateResponse from(Long postId, PostStatus postStatus){
        return new PostStatusUpdateResponse(postId, postStatus);
    }
}
