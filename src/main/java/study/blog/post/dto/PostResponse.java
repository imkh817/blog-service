package study.blog.post.dto;

import study.blog.post.entity.Post;
import study.blog.post.entity.PostTag;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record PostResponse(
        Long postId,
        Long authorId,
        String title,
        String content,
        PostStatus postStatus,
        long likeCount,
        boolean isLikedByMe,
        long viewCount,
        List<String> tags
) {
    public static PostResponse from(Post post, boolean isLikedByMe){
        return new PostResponse(post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getLikeCount(),
                isLikedByMe,
                post.getViewCount(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList()
                );
    }

    public static PostResponse from(Post post){
        return from(post, false);
    }
}
