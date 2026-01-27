package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "closure_folders")
public class ClosureFolder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closure_folder_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_folder_id", nullable = false)
    private Folder ancestorFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_folder_id", nullable = false)
    private Folder descendantFolder;

    @Column(name = "depth", nullable = false)
    private int depth;
}
