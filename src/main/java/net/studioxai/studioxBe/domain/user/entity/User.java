package net.studioxai.studioxBe.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.studioxai.studioxBe.domain.user.entity.enums.RegisterPath;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "register_path", nullable = false)
    private RegisterPath registerPath;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "email_verified_at", nullable = true)
    private LocalDateTime emailVerifiedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private User(RegisterPath registerPath, String email, String password, String profileImage, String username, boolean isEmailVerified, LocalDateTime emailVerifiedAt) {
        this.registerPath = registerPath;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.username = username;
        this.isEmailVerified = isEmailVerified;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public static User create(RegisterPath registerPath, String email, String password, String profileImage, String username, boolean isEmailVerified, LocalDateTime emailVerifiedAt) {
        return User.builder()
                .registerPath(registerPath)
                .email(email)
                .password(password)
                .profileImage(profileImage)
                .username(username)
                .isEmailVerified(isEmailVerified)
                .emailVerifiedAt(emailVerifiedAt)
                .build();
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

}
