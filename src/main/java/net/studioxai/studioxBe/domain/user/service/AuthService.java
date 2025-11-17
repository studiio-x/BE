package net.studioxai.studioxBe.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.user.dto.LoginRequest;
import net.studioxai.studioxBe.domain.user.dto.LoginResponse;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = getUserByEmailOrThrow(loginRequest.email());
        validatePassword(loginRequest.password(), user.getPassword());

        return buildLoginResponse(user);
    }
//
//    @Transactional
//    public LoginResponse signUp(LoginRequest loginRequest) {
//        // TODO: 이메일 검증 됐는지 확인
//
//        // TODO: 회원가입
//
//        // TODO: dto 반환
//
//    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserExceptionHandler(UserErrorCode.WRONG_ID_OR_PASSWORD)
        );
    }

    private void validatePassword(String inputPassword, String dbPassword) {
        if (!passwordEncoder.matches(inputPassword, dbPassword)) {
            throw new UserExceptionHandler(UserErrorCode.WRONG_ID_OR_PASSWORD);
        };
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return LoginResponse.create(
                user.getId(),
                user.getEmail(),
                user.getProfileImage(),
                accessToken,
                refreshToken
        );
    }

}
