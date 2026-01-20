package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.enums.FolderType;
import net.studioxai.studioxBe.domain.folder.entity.enums.InheritMode;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "folders")
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder", nullable = true)
    private Folder parentFolder;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false)
    private FolderType folderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "inherit_mode", nullable = false)
    private InheritMode inheritMode;

    public static Folder createSub(String name, Folder parentFolder) {
        if(parentFolder == null) {
            throw new FolderExceptionHandler(FolderErrorCode.PARENT_REQUIRED);
        }

        return Folder.builder()
                .name(name)
                .parentFolder(parentFolder)
                .folderType(FolderType.SUB)
                .build();
    }

    public static Folder createRoot(String name) {
        return Folder.builder()
                .name(name)
                .parentFolder(null)
                .folderType(FolderType.ROOT)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Folder(String name, Folder parentFolder, FolderType folderType) {
        this.name = name;
        this.parentFolder = parentFolder;
        this.folderType = folderType;
        this.inheritMode = InheritMode.INHERIT;
    }


}
