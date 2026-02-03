package study.blog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import study.blog.post.enums.PostStatus;

import java.util.List;

public record CreatePostDto(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @NotNull(message = "게시글 상태는 필수입니다.")
        PostStatus postStatus,

        @NotEmpty(message = "태그는 최소 1개 이상 등록해야 합니다.")
        @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
        List<String> tagNames
)
{

}
