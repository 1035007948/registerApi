-- ========================================
-- 用户注册系统 - PostgreSQL 数据库部署脚本
-- 版本: 1.0.0
-- 数据库: PostgreSQL 9.6+
-- ========================================

-- 创建数据库（需要超级用户权限）
-- CREATE DATABASE register_db 
-- WITH ENCODING='UTF8' 
-- LC_COLLATE='en_US.UTF-8' 
-- LC_CTYPE='en_US.UTF-8';

-- 连接到数据库
-- \c register_db

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- 创建用户表
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加表注释
COMMENT ON TABLE users IS '用户信息表';
COMMENT ON COLUMN users.id IS '用户ID';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password IS '密码(BCrypt加密)';
COMMENT ON COLUMN users.email IS '邮箱地址';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

-- ========================================
-- 创建索引
-- ========================================
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ========================================
-- 插入测试数据
-- ========================================
INSERT INTO users (username, password, email) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com')
ON CONFLICT (username) DO NOTHING;

-- ========================================
-- 创建视图
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
-- 创建函数 - 用户统计
-- ========================================
CREATE OR REPLACE FUNCTION fn_user_statistics()
RETURNS TABLE(
    total_users BIGINT,
    today_new_users BIGINT,
    week_new_users BIGINT,
    month_new_users BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) AS total_users,
        COUNT(CASE WHEN DATE(created_at) = CURRENT_DATE THEN 1 END) AS today_new_users,
        COUNT(CASE WHEN created_at >= CURRENT_DATE - INTERVAL '7 days' THEN 1 END) AS week_new_users,
        COUNT(CASE WHEN created_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) AS month_new_users
    FROM users;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 创建触发器函数 - 自动更新时间戳
-- ========================================
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS tr_user_update_timestamp ON users;
CREATE TRIGGER tr_user_update_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

-- ========================================
-- 验证表结构
-- ========================================
\d users

-- 查询测试数据
SELECT * FROM v_user_info LIMIT 10;

-- 执行统计函数
SELECT * FROM fn_user_statistics();

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库部署完成!' AS message;
