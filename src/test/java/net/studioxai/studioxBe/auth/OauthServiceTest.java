package net.studioxai.studioxBe.auth;

import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleUserInfoResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginTokenResult;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.auth.service.GoogleOauth;
import net.studioxai.studioxBe.domain.auth.service.OauthService;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OauthServiceTest {

    @Mock
    private GoogleOauth googleOauth;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OauthService oauthService;

    @Test
    @DisplayName("구글 로그인 성공 - 기존 유저")
    void googleLogin_success_existingUser() {
        // given
        String code = "auth-code";
        String googleSub = "google-sub";
        String email = "google@test.com";
        String name = "googleUser";
        String profileImage = "https://img.com/profile.png";

        Long userId = 1L;
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        GoogleTokenResponse tokenResponse = mock(GoogleTokenResponse.class);
        given(tokenResponse.getAccessToken()).willReturn("google-access-token");

        GoogleUserInfoResponse userInfo = mock(GoogleUserInfoResponse.class);
        given(userInfo.getSub()).willReturn(googleSub);

        User user = User.create(
                RegisterPath.GOOGLE,
                email,
                "encoded-password",
                profileImage,
                name,
                true,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "id", userId);

        given(googleOauth.requestAccessToken(code)).willReturn(tokenResponse);
        given(googleOauth.requestUserInfo("google-access-token")).willReturn(userInfo);
        given(userRepository.findByGoogleSub(googleSub)).willReturn(Optional.of(user));
        given(jwtProvider.createAccessToken(userId)).willReturn(accessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(refreshToken);

        // when
        LoginTokenResult result = oauthService.handleGoogleLogin(code);

        // then
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtProvider).createAccessToken(userId);
        verify(jwtProvider).createRefreshToken(userId);
    }

    @Test
    @DisplayName("구글 로그인 성공 - 신규 유저")
    void googleLogin_success_newUser() {
        // given
        String code = "auth-code";
        String googleSub = "new-google-sub";
        String email = "new@test.com";
        String name = "newUser";
        String profileImage = null;

        Long userId = 10L;
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        GoogleTokenResponse tokenResponse = mock(GoogleTokenResponse.class);
        given(tokenResponse.getAccessToken()).willReturn("google-access-token");

        GoogleUserInfoResponse userInfo = mock(GoogleUserInfoResponse.class);
        given(userInfo.getSub()).willReturn(googleSub);
        given(userInfo.getEmail()).willReturn(email);
        given(userInfo.getName()).willReturn(name);
        given(userInfo.getProfileImage()).willReturn(profileImage);

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

        given(jwtProvider.createAccessToken(userId)).willReturn(accessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(refreshToken);

        // when
        LoginTokenResult result = oauthService.handleGoogleLogin(code);

        // then
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
        verify(jwtProvider).createAccessToken(userId);
        verify(jwtProvider).createRefreshToken(userId);
    }

    @Test
    @DisplayName("구글 로그인 실패 - 인가 코드 누락")
    void googleLogin_fail_codeMissing() {
        // when
        AuthExceptionHandler ex = org.junit.jupiter.api.Assertions.assertThrows(
                AuthExceptionHandler.class,
                () -> oauthService.handleGoogleLogin(null)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.GOOGLE_AUTH_CODE_MISSING);
        verifyNoInteractions(googleOauth);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtProvider);
    }
}
