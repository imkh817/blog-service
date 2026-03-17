package study.blog.global.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.infra.LinkPreviewResponse;
import study.blog.global.infra.LinkPreviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/link-preview")
public class LinkPreviewController {

    private final LinkPreviewService linkPreviewService;

    @GetMapping
    public ApiResponse<LinkPreviewResponse> getPreview(@RequestParam String url) {
        return ApiResponse.success(linkPreviewService.fetchPreview(url));
    }
}
