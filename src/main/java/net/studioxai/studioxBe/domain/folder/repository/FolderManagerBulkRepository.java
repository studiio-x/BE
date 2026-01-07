package net.studioxai.studioxBe.domain.folder.repository;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FolderManagerBulkRepository {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveAllByBulk(List<User> users, Folder folder) {
        String sql = "INSERT INTO folder_managers (user_id, folder_id, created_at, updated_at) " +
                "VALUES (?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(sql,
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    User user = users.get(i);
                    ps.setLong(1, user.getId());
                    ps.setLong(2, folder.getId());
                }

                @Override
                public int getBatchSize() {
                    return users.size();
                }
        });
    }

    
}
