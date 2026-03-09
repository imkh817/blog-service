package study.blog.global.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.infra.PresignedUrlRequest;
import study.blog.global.infra.PresignedUrlResponse;
import study.blog.global.infra.S3ImageUploader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

    private final S3ImageUploader s3ImageUploader;

    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = s3ImageUploader.generatePresignedUrl(
                request.fileName(), request.contentType(), request.imageType()
        );
        return ApiResponse.success(response);
    }
}
