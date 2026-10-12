package com.campusfind.repository;

import com.campusfind.model.AdminAuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AdminAuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AdminAuditLog> auditRowMapper = (rs, rowNum) -> {
        AdminAuditLog log = new AdminAuditLog();
        log.setId(rs.getLong("id"));
        log.setAdminId(rs.getLong("admin_id"));
        log.setAction(rs.getString("action"));
        log.setTargetItemId(rs.getLong("target_item_id"));
        log.setReason(rs.getString("reason"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }
        try {
            log.setAdminEmail(rs.getString("admin_email"));
        } catch (Exception ignored) {}
        return log;
    };

    public void logAction(Long adminId, String action, Long targetItemId, String reason) {
        String sql = "INSERT INTO admin_audit_logs (admin_id, action, target_item_id, reason) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, adminId, action, targetItemId, reason);
    }

    public List<AdminAuditLog> findRecent(int limit) {
        String sql = "SELECT a.*, u.email AS admin_email " +
                     "FROM admin_audit_logs a " +
                     "LEFT JOIN users u ON a.admin_id = u.id " +
                     "ORDER BY a.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, auditRowMapper, limit);
    }
}
