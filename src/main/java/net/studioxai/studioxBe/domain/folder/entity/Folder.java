package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.enums.FolderType;
import net.studioxai.studioxBe.domain.folder.entity.enums.LinkMode;
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

    @Column(name = "acl_root_folder_id", nullable = true)
    private Long aclRootFolderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false)
    private FolderType folderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_mode", nullable = false)
    private LinkMode linkMode;

    public static Folder createSub(String name, Folder parentFolder) {
        if(parentFolder == null) {
            throw new FolderExceptionHandler(FolderErrorCode.PARENT_REQUIRED);
        }
        return Folder.builder()
                .name(name)
                .parentFolder(parentFolder)
                .folderType(FolderType.SUB)
                .linkMode(LinkMode.LINK)
                .aclRootFolderId(parentFolder.getAclRootFolderId())
                .build();
    }

    public static Folder createRoot(String name) {
        return Folder.builder()
                .name(name)
                .parentFolder(null)
                .folderType(FolderType.ROOT)
                .linkMode(LinkMode.UNLINK)
                .aclRootFolderId(null)
                .build();
    }

    public void updateRootAclId() {
        if (this.folderType != FolderType.ROOT) {
            throw new FolderExceptionHandler(FolderErrorCode.ACL_ROOT_SET_ONLY_FOR_ROOT);
        }
        this.aclRootFolderId = this.id;
    }

    public void updateLinkMode() {
        this.linkMode = this.linkMode.toggle();
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    public void move(Folder destinationFolder) {
        this.parentFolder = destinationFolder.getParentFolder();
        this.aclRootFolderId = destinationFolder.getAclRootFolderId();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Folder(
            String name,
            Folder parentFolder,
            FolderType folderType,
            LinkMode linkMode,
            Long aclRootFolderId
        ) {
        this.name = name;
        this.parentFolder = parentFolder;
        this.folderType = folderType;
        this.linkMode = linkMode;
        this.aclRootFolderId = aclRootFolderId;
    }




}
