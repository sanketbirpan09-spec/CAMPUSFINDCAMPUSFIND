package com.campusfind.repository;

import com.campusfind.model.PasswordResetToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PasswordResetToken> tokenRowMapper = (rs, rowNum) -> {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getLong("id"));
        token.setUserId(rs.getLong("user_id"));
        token.setTokenHash(rs.getString("token_hash"));
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            token.setExpiresAt(expiresAt.toLocalDateTime());
        }
        token.setUsed(rs.getBoolean("used"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            token.setCreatedAt(createdAt.toLocalDateTime());
        }
        return token;
    };

    public PasswordResetToken save(PasswordResetToken token) {
        String sql = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, used) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, token.getUserId());
            ps.setString(2, token.getTokenHash());
            ps.setTimestamp(3, Timestamp.valueOf(token.getExpiresAt()));
            ps.setBoolean(4, token.isUsed());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            token.setId(key.longValue());
        }
        return token;
    }

    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token_hash = ? AND used = FALSE";
        try {
            PasswordResetToken token = jdbcTemplate.queryForObject(sql, tokenRowMapper, tokenHash);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void markUsed(Long id) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void invalidateExistingTokensForUser(Long userId) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE user_id = ? AND used = FALSE";
        jdbcTemplate.update(sql, userId);
    }
}
