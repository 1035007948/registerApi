-- ========================================
-- 用户注册系统 - MySQL 数据库部署脚本
-- 版本: 1.0.0
-- 数据库: MySQL 5.7+
-- 字符集: UTF-8
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS register_db 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE register_db;

-- ========================================
-- 创建用户表
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一索引
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    
    -- 普通索引
    INDEX idx_created_at (created_at),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- ========================================
-- 插入测试数据
-- ========================================
-- 注意: 密码 'password123' 经过 BCrypt 加密后的值
INSERT INTO users (username, password, email) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com')
ON DUPLICATE KEY UPDATE username=username;

-- ========================================
-- 创建视图 - 用户基本信息视图
-- ========================================
CREATE OR REPLACE VIEW v_user_info AS
SELECT 
    id,
    username,
    email,
    created_at,
    updated_at
FROM users;

-- ========================================
-- 创建存储过程 - 用户统计
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_user_statistics()
BEGIN
    SELECT 
        COUNT(*) AS total_users,
        COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) AS today_new_users,
        COUNT(CASE WHEN created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN 1 END) AS week_new_users,
        COUNT(CASE WHEN created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) AS month_new_users
    FROM users;
END //

DELIMITER ;

-- ========================================
-- 创建触发器 - 自动更新时间戳
-- ========================================
DELIMITER //

CREATE TRIGGER tr_user_update_timestamp 
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

DELIMITER ;

-- ========================================
-- 创建索引优化查询性能
-- ========================================
-- 如果数据量大，可以添加更多索引
-- CREATE INDEX idx_username_email ON users(username, email);

-- ========================================
-- 授权（根据实际需求调整）
-- ========================================
-- 创建应用专用用户
-- CREATE USER IF NOT EXISTS 'register_app'@'%' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON register_db.* TO 'register_app'@'%';
-- FLUSH PRIVILEGES;

-- ========================================
-- 验证表结构
-- ========================================
DESCRIBE users;

-- 显示表创建语句
SHOW CREATE TABLE users\G

-- 查询测试数据
SELECT * FROM v_user_info LIMIT 10;

-- 执行统计存储过程
CALL sp_user_statistics();

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库部署完成!' AS message;
