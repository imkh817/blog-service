package study.blog.post.dto;

import study.blog.post.entity.Post;
import study.blog.post.entity.PostTag;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record ModifyPostResponse(
        Long postId,
        Long authorId,
        String title,
        String content,
        PostStatus postStatus,
        List<String> tags
) {
    public static ModifyPostResponse from(Post post){
        return new ModifyPostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList()
        );
    }
}
