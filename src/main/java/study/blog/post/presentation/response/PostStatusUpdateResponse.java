package study.blog.post.presentation.response;

import study.blog.post.domain.PostStatus;

public record PostStatusUpdateResponse(
        Long postId,
        PostStatus postStatus
) {

    public static PostStatusUpdateResponse from(Long postId, PostStatus postStatus){
        return new PostStatusUpdateResponse(postId, postStatus);
    }
}
