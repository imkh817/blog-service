package study.blog.post.presentation.response;

import study.blog.post.domain.PostStatus;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.entity.PostImage;
import study.blog.post.domain.entity.PostTag;

import java.util.List;

public record PostSaveResponse(
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
    public static PostSaveResponse from(Post post) {
        return new PostSaveResponse(
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
