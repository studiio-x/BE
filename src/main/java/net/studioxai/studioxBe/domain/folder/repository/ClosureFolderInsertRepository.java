package net.studioxai.studioxBe.domain.folder.repository;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.ClosureFolder;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ClosureFolderInsertRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void insertClosureForNewFolder(Long parentId, Long newFolderId) {
        if (parentId == null) {
            insertRootClosure(newFolderId);
        } else {
            insertChildClosure(parentId, newFolderId);
        }
    }

    private void insertRootClosure(Long newId) {
        String sql = """
            INSERT INTO closure_folders (ancestor_folder_id, descendant_folder_id, depth, created_at, updated_at)
            VALUES (:newId, :newId, 0, NOW(), NOW())
        """;
        jdbcTemplate.update(sql, Map.of("newId", newId));
    }

    private void insertChildClosure(Long parentId, Long newId) {
        String sql = """
            INSERT INTO closure_folders (ancestor_folder_id, descendant_folder_id, depth, created_at, updated_at)
            SELECT ancestor_folder_id, :newId, depth + 1, NOW(), NOW()
            FROM closure_folders
            WHERE descendant_folder_id = :parentId
            UNION ALL
            SELECT :newId, :newId, 0, NOW(), NOW()
        """;
        jdbcTemplate.update(sql, Map.of("parentId", parentId, "newId", newId));
    }
}