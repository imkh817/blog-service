package study.blog.global.infra;

public record PresignedUrlResponse(
        String presignedUrl,   // 클라이언트가 S3에 PUT 요청할 서명된 URL (5분 유효)
        String imageUrl        // 업로드 완료 후 사용할 최종 이미지 URL
) {}
