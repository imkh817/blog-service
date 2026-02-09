package study.blog.post.dto;

import study.blog.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostSearchCondition(
        String keyword, // 제목 + 내용 통합 검색
        List<String> tagNames, // 태그
        List<PostStatus> postStatuses, // 게시글 상태
        LocalDateTime createdFrom, // 작성일 시작
        LocalDateTime createdTo    // 작성일 종료
) {
}
