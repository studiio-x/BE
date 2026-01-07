package net.studioxai.studioxBe.auth;

import net.studioxai.studioxBe.domain.auth.dto.request.LoginRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.TokenResponse;
import net.studioxai.studioxBe.domain.project.service.ProjectService;
import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.auth.service.EmailVerificationService;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import net.studioxai.studioxBe.infra.redis.entity.Token;
import net.studioxai.studioxBe.infra.redis.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    @Mock
    private ProjectService projectService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    @DisplayName("로그인 성공 - 이메일/비밀번호 일치 시 토큰과 유저 정보 반환")
    void login_success() {
        // given
        String email = "test@example.com";
        String rawPassword = "plain-password";
        String encodedPassword = "encoded-password";
        Long userId = 1L;
        String profileImage = "https://example.com/profile.png";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String username = "username";

        LoginRequest loginRequest = new LoginRequest(email, rawPassword);

        User user = User.create(RegisterPath.CUSTOM, email, encodedPassword, profileImage, username, true, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", userId);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
        BDDMockito.given(jwtProvider.createAccessToken(userId)).willReturn(accessToken);
        BDDMockito.given(jwtProvider.createRefreshToken(userId)).willReturn(refreshToken);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        Assertions.assertThat(response.userId()).isEqualTo(userId);
        Assertions.assertThat(response.email()).isEqualTo(email);
        Assertions.assertThat(response.profileImageUrl()).isEqualTo(profileImage);
        Assertions.assertThat(response.accessToken()).isEqualTo(accessToken);
        Assertions.assertThat(response.refreshToken()).isEqualTo(refreshToken);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).matches(rawPassword, encodedPassword);
        Mockito.verify(jwtProvider).createAccessToken(userId);
        Mockito.verify(jwtProvider).createRefreshToken(userId);
        Mockito.verify(tokenService).saveRefreshToken(refreshToken, userId);
    }

    @Test
    @DisplayName("로그인 실패 - 이메일에 해당하는 유저가 없으면 예외 발생")
    void login_fail_userNotFound() {
        // given
        String email = "notfound@example.com";
        String rawPassword = "any";
        LoginRequest loginRequest = new LoginRequest(email, rawPassword);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        AuthExceptionHandler ex = org.junit.jupiter.api.Assertions.assertThrows(
                AuthExceptionHandler.class,
                () -> authService.login(loginRequest)
        );

        // then
        Assertions.assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.WRONG_ID_OR_PASSWORD);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder, Mockito.never()).matches(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(jwtProvider, Mockito.never()).createAccessToken(Mockito.anyLong());
        Mockito.verify(jwtProvider, Mockito.never()).createRefreshToken(Mockito.anyLong());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 예외 발생")
    void login_fail_wrongPassword() {
        // given
        String email = "test@example.com";
        String rawPassword = "wrong-password";
        String encodedPassword = "encoded-password";
        Long userId = 1L;

        String profileImage = "https://example.com/profile.png";
        String username = "username";

        LoginRequest loginRequest = new LoginRequest(email, rawPassword);

        User user = User.create(RegisterPath.CUSTOM, email, encodedPassword, profileImage, username, true, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", userId);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

        // when
        AuthExceptionHandler ex = org.junit.jupiter.api.Assertions.assertThrows(
                AuthExceptionHandler.class,
                () -> authService.login(loginRequest)
        );

        // then
        Assertions.assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.WRONG_ID_OR_PASSWORD);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).matches(rawPassword, encodedPassword);
        Mockito.verify(jwtProvider, Mockito.never()).createAccessToken(Mockito.anyLong());
        Mockito.verify(jwtProvider, Mockito.never()).createRefreshToken(Mockito.anyLong());
    }

    @Test
    @DisplayName("로그인 실패 - 가입 경로가 CUSTOM이 아니면 예외 발생")
    void login_fail_invalidRegisterPath() {
        // given
        String email = "social@example.com";
        String rawPassword = "plain-password";
        String encodedPassword = "encoded-password";
        Long userId = 1L;

        User user = User.create(RegisterPath.GOOGLE, email, encodedPassword, "profile", "username", true, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", userId);

        LoginRequest request = new LoginRequest(email, rawPassword);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        AuthExceptionHandler ex = org.junit.jupiter.api.Assertions.assertThrows(
                AuthExceptionHandler.class,
                () -> authService.login(request)
        );

        // then
        Assertions.assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_LOGIN_PATH);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder, Mockito.never()).matches(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(jwtProvider, Mockito.never()).createAccessToken(Mockito.anyLong());
    }

    @Test
    @DisplayName("회원가입 성공 - username 추출, 인코딩, 토큰 반환")
    void signUp_success() {
        // given
        String email = "newuser@example.com";
        String rawPassword = "plain";
        String encodedPassword = "encoded";
        Long newUserId = 10L;
        String accessToken = "new-access";
        String refreshToken = "new-refresh";

        LoginRequest request = new LoginRequest(email, rawPassword);

        Mockito.doNothing().when(emailVerificationService).checkEmailVerification(email);

        BDDMockito.given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);

        BDDMockito.given(userRepository.save(Mockito.any(User.class)))
                .willAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", newUserId);
                    return saved;
                });

        BDDMockito.given(jwtProvider.createAccessToken(newUserId)).willReturn(accessToken);
        BDDMockito.given(jwtProvider.createRefreshToken(newUserId)).willReturn(refreshToken);

        // when
        LoginResponse response = authService.signUp(request);

        // then
        Assertions.assertThat(response.userId()).isEqualTo(newUserId);
        Assertions.assertThat(response.email()).isEqualTo(email);
        Assertions.assertThat(response.accessToken()).isEqualTo(accessToken);
        Assertions.assertThat(response.refreshToken()).isEqualTo(refreshToken);

        Mockito.verify(emailVerificationService).checkEmailVerification(email);
        Mockito.verify(passwordEncoder).encode(rawPassword);

        Mockito.verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        Assertions.assertThat(savedUser.getEmail()).isEqualTo(email);
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("newuser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        Assertions.assertThat(savedUser.getRegisterPath()).isEqualTo(RegisterPath.CUSTOM);

        Mockito.verify(tokenService).saveRefreshToken(refreshToken, newUserId);
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() {
        // given
        String oldRt = "old";
        Long userId = 1L;
        String newAt = "new-at";
        String newRt = "new-rt";

        Token token = Mockito.mock(Token.class);
        BDDMockito.given(token.getUserId()).willReturn(userId);

        BDDMockito.given(tokenService.findByRefreshTokenOrThrow(oldRt)).willReturn(token);
        BDDMockito.given(jwtProvider.createAccessToken(userId)).willReturn(newAt);
        BDDMockito.given(jwtProvider.createRefreshToken(userId)).willReturn(newRt);

        // when
        TokenResponse response = authService.reissue(oldRt);

        // then
        Assertions.assertThat(response.accessToken()).isEqualTo(newAt);
        Assertions.assertThat(response.refreshToken()).isEqualTo(newRt);

        Mockito.verify(tokenService).findByRefreshTokenOrThrow(oldRt);
        Mockito.verify(jwtProvider).createAccessToken(userId);
        Mockito.verify(jwtProvider).createRefreshToken(userId);
        Mockito.verify(tokenService).saveRefreshToken(newRt, userId);
    }
}
