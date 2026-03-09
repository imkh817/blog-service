package study.blog.global.infra;

public record PresignedUrlRequest(
        String fileName,
        String contentType,
        String imageType   // "thumbnail" | "post-image"
) {}
