package net.studioxai.studioxBe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleTokenResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.GoogleUserInfoResponse;
import net.studioxai.studioxBe.domain.auth.dto.response.LoginTokenResult;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.global.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class OauthService {

    private final GoogleOauth googleOauth;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    //구글 로그인 페이지로 리다이렉트
    public String getGoogleLoginUrl() {
        return googleOauth.getOauthRedirectURL();
    }

    // 인가 코드로 access 요청
    public LoginTokenResult handleGoogleLogin(String code) {

        if (code == null || code.isBlank()) {
            throw new AuthExceptionHandler(AuthErrorCode.GOOGLE_AUTH_CODE_MISSING);
        }

        //access token 발급
        GoogleTokenResponse tokenResponse = googleOauth.requestAccessToken(code);

        //사용자 정보 조회
        GoogleUserInfoResponse userInfo = googleOauth.requestUserInfo(tokenResponse.getAccessToken());

        Optional<User> userOpt =
                userRepository.findByGoogleSub(userInfo.getSub());

        User user = userOpt.orElseGet(() -> {
                    String profileImage = userInfo.getProfileImage();
                    if (profileImage == null || profileImage.isBlank()) {
                        profileImage = "https://your-cdn.com/default-profile.png";
                    }

                 return userRepository.save(
                        User.createGoogleUser(
                                userInfo.getSub(),
                                userInfo.getEmail(),
                                userInfo.getName(),
                                passwordEncoder.encode(UUID.randomUUID().toString()),
                                profileImage
                        )
                );
    });

        //jwt 발급
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        log.info("ACCESS TOKEN = {}", accessToken);
        log.info("REFRESH TOKEN = {}", refreshToken);

        return new LoginTokenResult(accessToken, refreshToken);
    }


}

