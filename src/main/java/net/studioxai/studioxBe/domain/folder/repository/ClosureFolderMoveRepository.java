package net.studioxai.studioxBe.domain.folder.repository;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.exception.FolderErrorCode;
import net.studioxai.studioxBe.domain.folder.exception.FolderExceptionHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ClosureFolderMoveRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean existsPath(Long ancestorId, Long descendantId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM closure_folders
                WHERE ancestor_folder_id = :ancestorId
                  AND descendant_folder_id = :descendantId
                  AND depth > 0
            ) AS is_exists
        """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Map.of("ancestorId", ancestorId, "descendantId", descendantId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    public int deleteOldAncestorLinks(Long movingRootId) {
        String sql = """
            DELETE cf
            FROM closure_folders cf
            JOIN closure_folders sub
              ON cf.descendant_folder_id = sub.descendant_folder_id
            JOIN closure_folders anc
              ON cf.ancestor_folder_id = anc.ancestor_folder_id
            WHERE sub.ancestor_folder_id = :movingRootId
              AND anc.descendant_folder_id = :movingRootId
              AND anc.depth > 0
        """;
        return jdbcTemplate.update(sql, Map.of("movingRootId", movingRootId));
    }

    public int insertNewAncestorLinks(Long movingRootId, Long newParentId) {
        String sql = """
            INSERT INTO closure_folders (ancestor_folder_id, descendant_folder_id, depth, created_at, updated_at)
            SELECT
              newAnc.ancestor_folder_id,
              sub.descendant_folder_id,
              newAnc.depth + sub.depth + 1,
              NOW(), NOW()
            FROM closure_folders newAnc
            JOIN closure_folders sub
            WHERE newAnc.descendant_folder_id = :newParentId
              AND sub.ancestor_folder_id = :movingRootId
        """;
        return jdbcTemplate.update(sql, Map.of("movingRootId", movingRootId, "newParentId", newParentId));
    }

}
