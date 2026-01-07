package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Builder(access = AccessLevel.PRIVATE)
    public FolderManager(User user, Folder folder) {
        this.user = user;
        this.folder = folder;
    }

    public static FolderManager create(User user, Folder folder) {
        return FolderManager.builder()
                .user(user)
                .folder(folder)
                .build();
    }
}
