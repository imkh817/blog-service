package study.blog.post.dto;

import study.blog.post.entity.Post;
import study.blog.post.entity.PostImage;
import study.blog.post.entity.PostTag;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record PostResponse(
        Long postId,
        Long authorId,
        String authorNickname,
        String title,
        String content,
        PostStatus postStatus,
        long likeCount,
        boolean isLikedByMe,
        long viewCount,
        List<String> tags,
        long commentCounts,
        String thumbnailUrl,
        List<String> imageUrls
) {
    public static PostResponse from(Post post, String authorNickname, boolean isLikedByMe, long commentCounts){
        return new PostResponse(post.getId(),
                post.getAuthorId(),
                authorNickname,
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getLikeCount(),
                isLikedByMe,
                post.getViewCount(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList(),
                commentCounts,
                post.getThumbnailUrl(),
                post.getPostImages().stream()
                        .map(PostImage::getImageUrl)
                        .toList()
        );
    }

    public static PostResponse from(Post post, long viewCount, String authorNickname, boolean isLikedByMe, long commentCounts){
        return new PostResponse(post.getId(),
                post.getAuthorId(),
                authorNickname,
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getLikeCount(),
                isLikedByMe,
                viewCount,
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList(),
                commentCounts,
                post.getThumbnailUrl(),
                post.getPostImages().stream()
                        .map(PostImage::getImageUrl)
                        .toList()
        );
    }
}