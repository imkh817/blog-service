package study.blog.post.presentation.response;

import study.blog.post.domain.PostStatus;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.entity.PostTag;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        Long authorId,
        String authorNickname,
        String title,
        String content,
        PostStatus postStatus,
        String thumbnailUrl,
        List<String> tags,
        long likeCount,
        boolean isLikedByMe,
        long commentCounts,
        LocalDateTime createdAt
) {
    public static PostDetailResponse from(Post post, String authorNickname, boolean isLikedByMe, long commentCounts) {
        return new PostDetailResponse(
                post.getId(),
                post.getAuthorId(),
                authorNickname,
                post.getTitle(),
                post.getContent(),
                post.getPostStatus(),
                post.getThumbnailUrl(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList(),
                post.getLikeCount(),
                isLikedByMe,
                commentCounts,
                post.getCreatedAt()
        );
    }
}
