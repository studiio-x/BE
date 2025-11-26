package net.studioxai.studioxBe.user;

import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.user.dto.request.ProfileUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.request.UsernameUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.response.MypageResponse;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.domain.user.service.UserService;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3UrlHandler s3UrlHandler;

    @InjectMocks
    private UserService userService;

    private User createUser(String profileImage) {
        String email = "test@example.com";
        String rawPassword = "plain-password";
        String encodedPassword = "encoded-password";
        Long userId = 1L;
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String username = "username";

        User user = User.create(RegisterPath.CUSTOM, email, encodedPassword, profileImage, username, true, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    @Test
    @DisplayName("getUserByIdOrThrow - 존재하는 유저 찾기 성공")
    void getUserByIdOrThrow_success() {
        User user = createUser("origin-image");

        org.mockito.Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByIdOrThrow(1L);

        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByIdOrThrow - 유저 없을 때 예외 발생")
    void getUserByIdOrThrow_fail() {
        org.mockito.Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        UserExceptionHandler exception = assertThrows(
                UserExceptionHandler.class,
                () -> userService.getUserByIdOrThrow(1L)
        );

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("findUserDetail - 마이페이지 정보 조회 성공")
    void findUserDetail_success() {
        User user = createUser("origin-image");

        org.mockito.Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        MypageResponse response = userService.findUserDetail(1L);

        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("username", response.username());
        assertEquals("origin-image", response.profileImage());
    }

    @Test
    @DisplayName("updateUserProfile - 기존 이미지가 기본 이미지가 아니면 프로필 이미지가 변경된다")
    void updateUserProfile_whenNotDefaultImage_updatesProfileAndDeletesOld() {
        // given
        User user = createUser("origin-image");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ProfileUpdateRequest req = new ProfileUpdateRequest("new-image");

        // when
        userService.updateUserProfile(1L, req);

        // then
        assertEquals("new-image", user.getProfileImage());

        Mockito.verify(s3UrlHandler).delete("origin-image");
    }

    @Test
    @DisplayName("updateUserProfile - 기존 이미지가 기본 이미지이면 프로필 이미지는 변경되지 않는다")
    void updateUserProfile_whenDefaultImage_doesNotUpdateProfileButDeletesOld() {
        // given
        User user = createUser(AuthService.DEFAULT_PROFILE_IMAGE_URL);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ProfileUpdateRequest req = new ProfileUpdateRequest("new-image");

        // when
        userService.updateUserProfile(1L, req);

        // then
        assertEquals(AuthService.DEFAULT_PROFILE_IMAGE_URL, user.getProfileImage());

        Mockito.verify(s3UrlHandler).delete(AuthService.DEFAULT_PROFILE_IMAGE_URL);
    }

    @Test
    @DisplayName("getProfileImageUrl - S3 presigned URL 발급")
    void getProfileImageUrl_success() {
        S3Url url = S3Url.to("https://example-s3.com/presigned");

        org.mockito.Mockito.when(s3UrlHandler.handle("/profile")).thenReturn(url);

        S3Url result = userService.getProfileImageUrl();

        assertEquals("https://example-s3.com/presigned", result.getUrl());
    }
}