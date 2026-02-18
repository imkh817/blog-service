package study.blog.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.member.service.MemberService;
import study.blog.member.dto.MemberResponse;
import study.blog.member.dto.SignupRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo(Long memberId) {
        MemberResponse response = memberService.findMember(memberId);
        return ApiResponse.success(response);
    }
}
