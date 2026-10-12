package com.campusfind.repository;

import com.campusfind.model.Item;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Item> itemRowMapper = (rs, rowNum) -> {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setTitle(rs.getString("title"));
        item.setType(rs.getString("type"));
        item.setCategory(rs.getString("category"));
        item.setDescription(rs.getString("description"));
        Date itemDate = rs.getDate("item_date");
        if (itemDate != null) {
            item.setItemDate(itemDate.toLocalDate());
        }
        item.setLocation(rs.getString("location"));
        item.setContactInfo(rs.getString("contact_info"));
        item.setImageUrl(rs.getString("image_url"));
        item.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        // Joined reporter info if present
        try {
            item.setReporterName(rs.getString("reporter_name"));
            item.setReporterEmail(rs.getString("reporter_email"));
        } catch (Exception ignored) {}
        return item;
    };

    public Item save(Item item) {
        String sql = "INSERT INTO items (user_id, title, type, category, description, item_date, location, contact_info, image_url, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, item.getUserId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getType());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getDescription());
            ps.setDate(6, Date.valueOf(item.getItemDate()));
            ps.setString(7, item.getLocation());
            ps.setString(8, item.getContactInfo());
            ps.setString(9, item.getImageUrl());
            ps.setString(10, item.getStatus() != null ? item.getStatus() : "OPEN");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            item.setId(key.longValue());
        }
        return item;
    }

    public Optional<Item> findById(Long id) {
        String sql = "SELECT i.*, u.full_name AS reporter_name, u.email AS reporter_email " +
                     "FROM items i " +
                     "LEFT JOIN users u ON i.user_id = u.id " +
                     "WHERE i.id = ?";
        try {
            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper, id);
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Item> findRecent(int limit) {
        String sql = "SELECT i.*, u.full_name AS reporter_name, u.email AS reporter_email " +
                     "FROM items i " +
                     "LEFT JOIN users u ON i.user_id = u.id " +
                     "WHERE i.status != 'REMOVED_BY_ADMIN' " +
                     "ORDER BY i.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, itemRowMapper, limit);
    }

    public List<Item> findByUserId(Long userId) {
        String sql = "SELECT i.*, u.full_name AS reporter_name, u.email AS reporter_email " +
                     "FROM items i " +
                     "LEFT JOIN users u ON i.user_id = u.id " +
                     "WHERE i.user_id = ? AND i.status != 'REMOVED_BY_ADMIN' " +
                     "ORDER BY i.created_at DESC";
        return jdbcTemplate.query(sql, itemRowMapper, userId);
    }

    public List<Item> searchAndFilter(String query, String type, String category, String location, String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.*, u.full_name AS reporter_name, u.email AS reporter_email " +
                "FROM items i " +
                "LEFT JOIN users u ON i.user_id = u.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND i.status = ? ");
            params.add(status.toUpperCase());
        } else {
            sql.append("AND i.status != 'REMOVED_BY_ADMIN' ");
        }

        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND i.type = ? ");
            params.add(type.toUpperCase());
        }

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND i.category = ? ");
            params.add(category);
        }

        if (location != null && !location.isBlank() && !location.equalsIgnoreCase("ALL")) {
            sql.append("AND i.location LIKE ? ");
            params.add("%" + location.trim() + "%");
        }

        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.location) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append("ORDER BY i.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), itemRowMapper, params.toArray());
    }

    public long countSearchAndFilter(String query, String type, String category, String location, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM items i WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND i.status = ? ");
            params.add(status.toUpperCase());
        } else {
            sql.append("AND i.status != 'REMOVED_BY_ADMIN' ");
        }

        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND i.type = ? ");
            params.add(type.toUpperCase());
        }

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND i.category = ? ");
            params.add(category);
        }

        if (location != null && !location.isBlank() && !location.equalsIgnoreCase("ALL")) {
            sql.append("AND i.location LIKE ? ");
            params.add("%" + location.trim() + "%");
        }

        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.location) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    public void updateStatus(Long itemId, String status) {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, itemId);
    }

    public void deleteById(Long itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        jdbcTemplate.update(sql, itemId);
    }

    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM items WHERE status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status);
        return count != null ? count : 0L;
    }

    public long countByType(String type) {
        String sql = "SELECT COUNT(*) FROM items WHERE type = ? AND status != 'REMOVED_BY_ADMIN'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, type);
        return count != null ? count : 0L;
    }

    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM items WHERE status != 'REMOVED_BY_ADMIN'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    public List<Item> findAllForAdmin(int limit) {
        String sql = "SELECT i.*, u.full_name AS reporter_name, u.email AS reporter_email " +
                     "FROM items i " +
                     "LEFT JOIN users u ON i.user_id = u.id " +
                     "ORDER BY i.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, itemRowMapper, limit);
    }
}
