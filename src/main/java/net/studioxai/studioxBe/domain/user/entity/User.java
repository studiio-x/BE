package net.studioxai.studioxBe.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.studioxai.studioxBe.global.entity.BaseEntity;

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

    @Builder
    public User(RegisterPath registerPath, String email, String password, String profileImage, String username) {
        this.registerPath = registerPath;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.username = username;
    }

}
