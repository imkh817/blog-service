package study.blog.post.presentation.requset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveDraftRequest(

        Long postId, // null이면 신규 생성, 있으면 기존 임시저장 수정

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
        String title,

        String content,

        @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
        List<String> tagNames,

        String thumbnailUrl
) {}