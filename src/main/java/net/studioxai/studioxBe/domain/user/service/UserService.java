package net.studioxai.studioxBe.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.user.dto.MypageResponse;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.domain.user.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.user.exception.UserExceptionHandler;
import net.studioxai.studioxBe.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

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

}
