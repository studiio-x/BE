package net.studioxai.studioxBe.user;

import net.studioxai.studioxBe.domain.user.dto.LoginRequest;
import net.studioxai.studioxBe.domain.user.dto.LoginResponse;
import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.user.service.AuthService;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

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

        User user = User.create(RegisterPath.CUSTOM, email, encodedPassword, profileImage, username);
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
    }

    @Test
    @DisplayName("로그인 실패 - 이메일에 해당하는 유저가 없으면 예외 발생")
    void login_fail_userNotFound() {
        // given
        String email = "notfound@example.com";
        String rawPassword = "any";
        LoginRequest loginRequest = new LoginRequest(email, rawPassword);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        UserExceptionHandler ex = assertThrows(
                UserExceptionHandler.class,
                () -> authService.login(loginRequest)
        );

        Assertions.assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.WRONG_ID_OR_PASSWORD);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder, Mockito.never()).matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(jwtProvider, Mockito.never()).createAccessToken(ArgumentMatchers.anyLong());
        Mockito.verify(jwtProvider, Mockito.never()).createRefreshToken(ArgumentMatchers.anyLong());
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

        User user = User.create(RegisterPath.CUSTOM, email, encodedPassword, profileImage, username);
        ReflectionTestUtils.setField(user, "id", userId);

        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

        // when & then
        UserExceptionHandler ex = assertThrows(
                UserExceptionHandler.class,
                () -> authService.login(loginRequest)
        );

        Assertions.assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.WRONG_ID_OR_PASSWORD);

        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).matches(rawPassword, encodedPassword);
        Mockito.verify(jwtProvider, Mockito.never()).createAccessToken(ArgumentMatchers.anyLong());
        Mockito.verify(jwtProvider, Mockito.never()).createRefreshToken(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("회원가입 성공 - 비밀번호는 인코딩되고, username은 이메일 @ 앞부분으로 저장된다")
    void signUp_success() {
        // given
        String email = "newuser@example.com";
        String rawPassword = "plain-password";
        String encodedPassword = "encoded-password";
        String expectedUsername = "newuser";
        String defaultProfileUrl = "profile-example.com";
        Long generatedUserId = 10L;
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        LoginRequest signUpRequest = new LoginRequest(email, rawPassword);

        BDDMockito.given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);

        BDDMockito.given(userRepository.save(Mockito.any(User.class)))
                .willAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", generatedUserId);
                    return saved;
                });

        BDDMockito.given(jwtProvider.createAccessToken(generatedUserId)).willReturn(accessToken);
        BDDMockito.given(jwtProvider.createRefreshToken(generatedUserId)).willReturn(refreshToken);

        // when
        LoginResponse response = authService.signUp(signUpRequest);

        // then: 반환 DTO 검증
        Assertions.assertThat(response.userId()).isEqualTo(generatedUserId);
        Assertions.assertThat(response.email()).isEqualTo(email);
        Assertions.assertThat(response.accessToken()).isEqualTo(accessToken);
        Assertions.assertThat(response.refreshToken()).isEqualTo(refreshToken);

        Mockito.verify(passwordEncoder).encode(rawPassword);

        Mockito.verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        Assertions.assertThat(savedUser.getEmail()).isEqualTo(email);
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(expectedUsername);
        Assertions.assertThat(savedUser.getProfileImage()).isEqualTo(defaultProfileUrl);
        Assertions.assertThat(savedUser.getRegisterPath()).isEqualTo(RegisterPath.CUSTOM);

        Mockito.verify(jwtProvider).createAccessToken(generatedUserId);
        Mockito.verify(jwtProvider).createRefreshToken(generatedUserId);
    }
}
