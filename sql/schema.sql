-- ============================================================================
-- CampusFind — College Lost and Found Management System
-- Database Schema for MySQL 8.0+
-- ============================================================================

CREATE DATABASE IF NOT EXISTS campusfind_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE campusfind_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Items (Lost and Found Reports) Table
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    type VARCHAR(10) NOT NULL COMMENT 'LOST or FOUND',
    category VARCHAR(50) NOT NULL COMMENT 'Electronics, ID & Cards, Books, Keys & Wallets, Clothing, Other',
    description TEXT NOT NULL,
    item_date DATE NOT NULL,
    location VARCHAR(150) NOT NULL COMMENT 'Campus spot: Library, Cafeteria, Science Block, etc.',
    contact_info VARCHAR(255) NOT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, RESOLVED, REMOVED_BY_ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_items_type (type),
    INDEX idx_items_status (status),
    INDEX idx_items_category (category),
    INDEX idx_items_user_id (user_id),
    INDEX idx_items_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Password Reset Tokens Table (Expiring, Single-Use, Hashed)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_prt_token_hash (token_hash),
    INDEX idx_prt_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Admin Audit Logs Table
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_item_id BIGINT DEFAULT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_audit_admin (admin_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial Seed: Website Owner & Administrator (Sanket Birpan)
INSERT INTO users (full_name, email, password_hash, role, phone)
VALUES ('Sanket Birpan (Owner / Admin)', 'sanketbirpan09@gmail.com', '$2a$10$LUMlb7Lj1EMKDXBT2D6tZOJFnsVBYKmVFqc/SCsKJu6lDQGWMnfaO', 'ROLE_ADMIN', '+91-9876543210')
ON DUPLICATE KEY UPDATE role = 'ROLE_ADMIN';

