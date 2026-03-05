package study.blog.global.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    /**
     * Presigned URL 발급
     * 클라이언트가 서버를 거치지 않고 S3에 직접 업로드할 수 있는 서명된 URL을 반환한다.
     *
     * @param fileName    원본 파일명 (확장자 추출에 사용)
     * @param contentType 파일 MIME 타입
     * @param imageType   "thumbnail" | "post-image"
     */
    public PresignedUrlResponse generatePresignedUrl(String fileName, String contentType, String imageType) {
        validateExtension(fileName);

        String prefix = "thumbnail".equals(imageType) ? "thumbnail" : "post-images";
        String key = prefix + "/" + UUID.randomUUID() + "." + extractExtension(fileName);

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(p -> p
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                )
        );

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                buildUrl(key)
        );
    }

    private String buildUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private void validateExtension(String fileName) {
        String extension = extractExtension(fileName).toLowerCase();
        if (!extension.matches("jpg|jpeg|png|webp|gif")) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp, gif)");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
