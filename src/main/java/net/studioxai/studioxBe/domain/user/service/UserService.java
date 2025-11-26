package net.studioxai.studioxBe.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.auth.service.AuthService;
import net.studioxai.studioxBe.domain.user.dto.request.ProfileUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.request.UsernameUpdateRequest;
import net.studioxai.studioxBe.domain.user.dto.response.MypageResponse;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import net.studioxai.studioxBe.infra.s3.S3Url;
import net.studioxai.studioxBe.infra.s3.S3UrlHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final S3UrlHandler s3UrlHandler;
    private final UserRepository userRepository;

    public User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserExceptionHandler(UserErrorCode.USER_NOT_FOUND)
        );
    }

    public MypageResponse findUserDetail(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return MypageResponse.create(user.getId(), user.getEmail(), user.getUsername(), user.getProfileImage());

    }

    @Transactional
    public void updateUserProfile(Long userId, ProfileUpdateRequest profileUpdateRequest) {
        User user = getUserByIdOrThrow(userId);
        String originProfileImage = user.getProfileImage();

        if(!originProfileImage.equals(AuthService.DEFAULT_PROFILE_IMAGE_URL)) {
            user.updateProfileImage(profileUpdateRequest.profileImage());
        }

        s3UrlHandler.delete(originProfileImage);

    }

    @Transactional
    public void updateUsername(Long userId, UsernameUpdateRequest usernameUpdateRequest) {
        User user = getUserByIdOrThrow(userId);
        user.updateUsername(usernameUpdateRequest.username());
    }

    public S3Url getProfileImageUrl() {
        return s3UrlHandler.handle("/profile");
    }

}
