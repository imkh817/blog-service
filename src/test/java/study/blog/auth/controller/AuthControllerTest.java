package study.blog.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import study.blog.auth.dto.LoginRequest;
import study.blog.auth.dto.LoginResult;
import study.blog.auth.dto.ReissueResult;
import study.blog.auth.exception.InvalidRefreshTokenException;
import study.blog.auth.repository.TokenBlacklistRepository;
import study.blog.auth.service.AuthService;
import study.blog.global.config.JpaConfig;
import study.blog.global.config.SecurityConfig;
import study.blog.global.security.handler.CustomAccessDeniedHandler;
import study.blog.global.security.handler.CustomAuthenticationEntryPoint;
import study.blog.global.security.jwt.JwtTokenProvider;
import study.blog.global.security.principal.MemberUserDetailsService;
import study.blog.member.dto.MemberResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaConfig.class
        )
)
@Import(SecurityConfig.class)
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenBlacklistRepository tokenBlacklistRepository;

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    // ===== POST /api/v1/auth/login =====

    @Test
    @DisplayName("로그인 시 200과 AccessToken을 반환하고 쿠키를 설정한다")
    void login_200_응답() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        MemberResponse memberResponse = new MemberResponse(1L, "test@test.com", "테스터");
        LoginResult loginResult = new LoginResult("access-token", "refresh-token", memberResponse);

        given(authService.login(any(LoginRequest.class))).willReturn(loginResult);
        given(jwtTokenProvider.getRefreshExpirationInMs()).willReturn(604_800_000L);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@test.com"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("이메일이 비어 있으면 400 응답을 반환한다")
    void login_빈_이메일_400_응답() throws Exception {
        // given
        LoginRequest request = new LoginRequest("", "password123");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400 응답을 반환한다")
    void login_잘못된_이메일_형식_400_응답() throws Exception {
        // given
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 400 응답을 반환한다")
    void login_빈_비밀번호_400_응답() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/v1/auth/refresh =====

    @Test
    @DisplayName("유효한 refresh_token 쿠키로 재발급 시 200과 새 AccessToken을 반환한다")
    void refresh_200_응답() throws Exception {
        // given
        ReissueResult reissueResult = new ReissueResult("new-access-token", "new-refresh-token");

        given(authService.reissue("valid-refresh-token")).willReturn(reissueResult);
        given(jwtTokenProvider.getRefreshExpirationInMs()).willReturn(604_800_000L);

        MockCookie refreshCookie = new MockCookie("refresh_token", "valid-refresh-token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("refresh_token 쿠키가 없으면 401 응답을 반환한다")
    void refresh_쿠키_없음_401_응답() throws Exception {
        // when & then
        // 쿠키 없이 요청 → 컨트롤러에서 InvalidRefreshTokenException 발생 → GlobalExceptionHandler → 401
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken으로 재발급 시 401 응답을 반환한다")
    void refresh_유효하지_않은_토큰_401_응답() throws Exception {
        // given
        given(authService.reissue("invalid-token"))
                .willThrow(new InvalidRefreshTokenException("로그아웃된 사용자입니다."));

        MockCookie refreshCookie = new MockCookie("refresh_token", "invalid-token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/v1/auth/logout =====

    @Test
    @DisplayName("유효한 Authorization 헤더로 로그아웃 시 200 응답과 쿠키 만료를 반환한다")
    void logout_유효한_헤더_200_응답() throws Exception {
        // given
        willDoNothing().given(authService).logout("valid-access-token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().exists("Set-Cookie")); // maxAge=0 쿠키 삭제 확인
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 400 응답을 반환한다")
    void logout_헤더_없음_400_응답() throws Exception {
        // when & then
        // @RequestHeader("Authorization") 누락 → Spring MVC → 400
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest());
    }
}
