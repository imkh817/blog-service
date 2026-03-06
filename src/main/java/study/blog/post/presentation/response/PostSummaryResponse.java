package study.blog.post.presentation.response;

import study.blog.post.domain.PostStatus;
import study.blog.post.domain.entity.Post;
import study.blog.post.domain.entity.PostTag;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
        Long postId,
        String title,
        String authorNickname,
        String thumbnailUrl,
        List<String> tags,
        PostStatus postStatus,
        long likeCount,
        long commentCounts,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post, String authorNickname, long commentCounts) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                authorNickname,
                post.getThumbnailUrl(),
                post.getTags().stream()
                        .distinct()
                        .map(PostTag::getName)
                        .toList(),
                post.getPostStatus(),
                post.getLikeCount(),
                commentCounts,
                post.getCreatedAt()
        );
    }
}
