package study.blog.global.infra;

public record LinkPreviewResponse(
        String title,
        String description,
        String image,
        String favicon,
        String url
) {}
