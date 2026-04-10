-- ========================================================
-- 用户注册接口数据库部署脚本
-- 数据库: MySQL 5.7+
-- 字符集: utf8mb4
-- ========================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS register_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE register_db;

-- ========================================================
-- 用户表 (users)
-- ========================================================
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone_number VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常, INACTIVE-未激活, LOCKED-锁定, DELETED-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================================
-- 初始化数据
-- ========================================================

-- 插入测试用户 (密码: 123456, 已用BCrypt加密)
INSERT INTO users (username, password, email, phone_number, nickname, status, created_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'admin@example.com', '13800138000', '管理员', 'ACTIVE', NOW()),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'test@example.com', '13800138001', '测试用户', 'ACTIVE', NOW()),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'zhangsan@example.com', '13900139000', '张三', 'ACTIVE', NOW()),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'lisi@example.com', '13900139001', '李四', 'ACTIVE', NOW()),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'wangwu@example.com', '13900139002', '王五', 'INACTIVE', NOW());

-- ========================================================
-- 创建触发器: 自动更新 updated_at 字段
-- ========================================================
DELIMITER //

CREATE TRIGGER trg_users_before_update
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END//

DELIMITER ;

-- ========================================================
-- 验证数据
-- ========================================================
SELECT '用户表创建成功' AS message;
SELECT COUNT(*) AS total_users FROM users;
SELECT id, username, email, phone_number, nickname, status, created_at FROM users;
