package study.blog.global.web.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        LoginMember annotation = parameter.getParameterAnnotation(LoginMember.class);
        boolean required = (annotation == null) || annotation.required();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long memberId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginMemberId loginMemberId) {
                memberId = loginMemberId.memberId();
            }
        }

        if (memberId == null && required) {
            throw new IllegalArgumentException("인증 정보가 존재하지 않습니다.");
        }

        return memberId;
    }
}
