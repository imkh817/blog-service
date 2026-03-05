package study.blog.post.dto;

import study.blog.post.entity.Post;
import study.blog.post.entity.PostImage;
import study.blog.post.entity.PostTag;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record CreatePostResponse(
        Long postId,
        Long authorId,
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
    public static CreatePostResponse from(Post post){
        return new CreatePostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                0L,
                false,
                0L,
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList(),
                0L,
                post.getThumbnailUrl(),
                post.getPostImages().stream()
                        .map(PostImage::getImageUrl)
                        .toList()
        );
    }
}