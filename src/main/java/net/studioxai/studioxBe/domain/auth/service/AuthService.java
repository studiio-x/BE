package net.studioxai.studioxBe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.request.LoginRequest;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.TokenResponse;
import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import net.studioxai.studioxBe.infra.redis.entity.Token;
import net.studioxai.studioxBe.infra.redis.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;

    // TODO: 추후 로직 수정
    public static final String DEFAULT_PROFILE_IMAGE_URL = "profile-example.com";

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = getUserByEmailOrThrow(loginRequest.email());

        validateRegisterPath(user, RegisterPath.CUSTOM);

        validatePassword(loginRequest.password(), user.getPassword());

        return buildLoginResponse(user);
    }

    @Transactional
    public LoginResponse signUp(LoginRequest loginRequest) {
        emailVerificationService.checkEmailVerification(loginRequest.email());

        // TODO: default profile url 삽입
        String encodedPassword = passwordEncoder.encode(loginRequest.password());

        User user = User.create(
                RegisterPath.CUSTOM,
                loginRequest.email(),
                encodedPassword,
                DEFAULT_PROFILE_IMAGE_URL,
                extractUsernameFromEmail(loginRequest.email()),
                true,
                LocalDateTime.now()
        );

        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        Token token = tokenService.findByRefreshTokenOrThrow(refreshToken);
        Map<String, String> tokens = buildToken(token.getUserId());

        return TokenResponse.create(
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new AuthExceptionHandler(AuthErrorCode.WRONG_ID_OR_PASSWORD)
        );
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthExceptionHandler(AuthErrorCode.WRONG_ID_OR_PASSWORD);
        }
    }

    private LoginResponse buildLoginResponse(User user) {
        Map<String, String> tokens = buildToken(user.getId());

        return LoginResponse.create(
                user.getId(),
                user.getEmail(),
                user.getProfileImage(),
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
    }

    private Map<String, String> buildToken(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        tokenService.saveRefreshToken(refreshToken, userId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    private String extractUsernameFromEmail(String email) {
        int index = email.indexOf("@");
        return (index > 0) ? email.substring(0, index) : email;
    }

    private void validateRegisterPath(User user, RegisterPath registerPath) {
        if (user.getRegisterPath() != registerPath) {
            throw new AuthExceptionHandler(AuthErrorCode.INVALID_LOGIN_PATH);
        }
    }

}
