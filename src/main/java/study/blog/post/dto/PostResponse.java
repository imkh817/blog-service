package study.blog.post.dto;

import study.blog.post.entity.Post;
import study.blog.post.entity.PostTag;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record PostResponse(
        Long postId,
        String title,
        String content,
        PostStatus postStatus,
        long viewCount,
        List<String> tags
) {
    public static PostResponse from(Post post){
        PostResponse postResponse = new PostResponse(post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getViewCount(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList()
                );

        return postResponse;
    }
}
