package net.studioxai.studioxBe.auth;

import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleUserInfoResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginTokenResult;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.auth.service.GoogleOauth;
import net.studioxai.studioxBe.domain.auth.service.OauthService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OauthServiceTest {

    @Mock
    private GoogleOauth googleOauth;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthService authService;

    @InjectMocks
    private OauthService oauthService;

    @Test
    @DisplayName("구글 로그인 성공 - 기존 유저")
    void googleLogin_success_existingUser() {
        // given
        String code = "auth-code";
        String googleSub = "google-sub";
        Long userId = 1L;

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse(
                "google-access-token",
                3600,
                "Bearer",
                "profile email"
        );

        GoogleUserInfoResponse userInfo = mock(GoogleUserInfoResponse.class);
        given(userInfo.getSub()).willReturn(googleSub);

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        given(user.getEmail()).willReturn("google@test.com");
        given(user.getProfileImage()).willReturn("profile.png");

        given(googleOauth.requestAccessToken(code)).willReturn(tokenResponse);
        given(googleOauth.requestUserInfo("google-access-token")).willReturn(userInfo);
        given(userRepository.findByGoogleSub(googleSub)).willReturn(Optional.of(user));

        given(authService.issueTokens(userId)).willReturn(
                Map.of(
                        "accessToken", "access-token",
                        "refreshToken", "refresh-token"
                )
        );

        // when
        LoginResponse result = oauthService.loginWithGoogle(code);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        verify(userRepository, never()).save(any());
        verify(authService).issueTokens(userId);
    }

    @Test
    @DisplayName("구글 로그인 성공 - 신규 유저")
    void googleLogin_success_newUser() {
        // given
        String code = "auth-code";
        String googleSub = "new-google-sub";
        Long userId = 10L;

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse(
                "google-access-token",
                3600,
                "Bearer",
                "profile email"
        );

        GoogleUserInfoResponse userInfo = mock(GoogleUserInfoResponse.class);
        given(userInfo.getSub()).willReturn(googleSub);
        given(userInfo.getEmail()).willReturn("new@test.com");
        given(userInfo.getName()).willReturn("newUser");
        given(userInfo.getProfileImage()).willReturn(null);

        given(googleOauth.requestAccessToken(code)).willReturn(tokenResponse);
        given(googleOauth.requestUserInfo("google-access-token")).willReturn(userInfo);
        given(userRepository.findByGoogleSub(googleSub)).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");

        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", userId);
                    return saved;
                });

        given(authService.issueTokens(userId)).willReturn(
                Map.of(
                        "accessToken", "access-token",
                        "refreshToken", "refresh-token"
                )
        );

        // when
        LoginResponse result = oauthService.loginWithGoogle(code);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
        verify(authService).issueTokens(userId);
    }

    @Test
    @DisplayName("구글 로그인 실패 - 인가 코드 누락")
    void googleLogin_fail_codeMissing() {
        // when
        AuthExceptionHandler ex = assertThrows(
                AuthExceptionHandler.class,
                () -> oauthService.loginWithGoogle(null)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.GOOGLE_AUTH_CODE_MISSING);
        verifyNoInteractions(googleOauth, userRepository, authService);
    }
}
