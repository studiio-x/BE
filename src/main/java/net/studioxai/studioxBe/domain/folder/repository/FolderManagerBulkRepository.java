package net.studioxai.studioxBe.domain.folder.repository;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FolderManagerBulkRepository {
    private final JdbcTemplate jdbcTemplate;

    public void upsertManagersForFolder(Long folderId, List<FolderManagerDto> managers) {
        if (managers == null || managers.isEmpty()) return;

        String sql = """
            INSERT INTO folder_managers (folder_id, user_id, permission, link_mode, created_at, updated_at)
            VALUES (?, ?, ?, (SELECT link_mode FROM folders WHERE folder_id = ?), NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                permission = VALUES(permission),
                link_mode = VALUES(link_mode),
                updated_at = NOW()
            """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FolderManagerDto dto = managers.get(i);
                ps.setLong(1, folderId);
                ps.setLong(2, dto.userId());
                ps.setString(3, dto.permission().name()); // Permission enum name
                ps.setLong(4, folderId);
            }

            @Override
            public int getBatchSize() {
                return managers.size();
            }
        });
    }

    public void deleteManagersForFolder(Long folderId, List<FolderManagerDto> managers) {
        if (managers == null || managers.isEmpty()) return;

        String sql = """
            DELETE FROM folder_managers
            WHERE folder_id = ?
              AND user_id = ?
            """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, folderId);
                ps.setLong(2, managers.get(i).userId());
            }

            @Override
            public int getBatchSize() {
                return managers.size();
            }
        });
    }
}
