package net.studioxai.studioxBe.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.ErrorResponse;
import net.studioxai.studioxBe.global.error.GlobalErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 이미 인증되어 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = resolveToken(request);

            if (token != null) {
                try {
                    if (jwtProvider.validate(token)) {
                        if (jwtProvider.getCategory(token).equals("refresh")) {
                            log.info("리프레시 토큰으로 접근 시도");
                            writeErrorResponse(response, GlobalErrorCode.INVALID_TOKEN);
                            return;
                        }
                        String subject = jwtProvider.getSubject(token); // 로그인 시 subject = userId 로 발급했어야 함
                        Long userId = Long.parseLong(subject);

                        var principal = new JwtUserPrincipal(userId);
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // 필요시 롤 클레임에서 채우기

                        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }

                } catch (ExpiredJwtException e) {
                    log.info("만료된 토큰", e);
                    writeErrorResponse(response, GlobalErrorCode.EXPIRED_TOKEN);
                    return;

                } catch (JwtException | IllegalArgumentException e) {
                    log.info("유효하지 않은 토큰", e);
                    writeErrorResponse(response, GlobalErrorCode.INVALID_TOKEN);
                    return;

                } catch (Exception e) {
                    log.error("JWT 필터에서 처리되지 않은 예외", e);
                    writeErrorResponse(response, GlobalErrorCode.UNCAUGHT_EXCEPTION);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode) throws IOException {
        ErrorReason errorReason = errorCode.getErrorReason();      // status, code, reason
        ErrorResponse errorResponse = ErrorResponse.from(errorReason);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
