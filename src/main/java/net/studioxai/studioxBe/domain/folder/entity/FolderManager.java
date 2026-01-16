package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderManagerExceptionHandler;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "folder_managers")
public class FolderManager extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_manager_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="folder_id", nullable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Permission permission;

    @Builder(access = AccessLevel.PRIVATE)
    public FolderManager(User user, Folder folder, Permission permission) {
        this.user = user;
        this.folder = folder;
        this.permission = permission;
    }

    public static FolderManager createRootManager(User user, Folder folder) {
        return FolderManager.builder()
                .user(user)
                .folder(folder)
                .permission(Permission.OWNER)
                .build();
    }

    public static FolderManager createWriter(User user, Folder folder) {
        return FolderManager.builder()
                .user(user)
                .folder(folder)
                .permission(Permission.WRITE)
                .build();
    }

    public static FolderManager createReader(User user, Folder folder) {
        return FolderManager.builder()
                .user(user)
                .folder(folder)
                .permission(Permission.READ)
                .build();
    }

    public void updatePermission() {
        if (this.permission == Permission.READ) {
            this.permission = Permission.WRITE;
        }
        else {
            this.permission = Permission.READ;
        }
    }
}
