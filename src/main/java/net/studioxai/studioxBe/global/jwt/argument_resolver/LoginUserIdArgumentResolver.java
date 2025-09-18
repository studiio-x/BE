package net.studioxai.studioxBe.global.jwt.argument_resolver;

import net.studioxai.studioxBe.global.jwt.JwtUserPrincipal;
import net.studioxai.studioxBe.global.jwt.annotation.LoginUserId;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUserId.class)
                && (parameter.getParameterType() == Long.class || parameter.getParameterType() == long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        LoginUserId ann = parameter.getParameterAnnotation(LoginUserId.class);
        boolean required = (ann == null) || ann.required();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserPrincipal p) {
            return p.userId();
        }

        // 예상과 다른 Principal 타입이면 인증 실패 처리
        if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보를 확인할 수 없습니다.");
        return null;
    }
}
