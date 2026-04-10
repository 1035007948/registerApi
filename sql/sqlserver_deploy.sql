-- ========================================
-- 用户注册系统 - SQL Server 数据库部署脚本
-- 版本: 1.0.0
-- 数据库: SQL Server 2016+
-- ========================================

-- 创建数据库
CREATE DATABASE register_db;
GO

-- 使用数据库
USE register_db;
GO

-- ========================================
-- 创建用户表
-- ========================================
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- 添加表注释
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'用户信息表', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users';

-- 添加列注释
EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'用户ID', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'id';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'用户名', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'username';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'密码(BCrypt加密)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'password';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'邮箱地址', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'email';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'创建时间', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'created_at';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'更新时间', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'users',
    @level2type = N'COLUMN', @level2name = N'updated_at';
GO

-- ========================================
-- 创建索引
-- ========================================
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_email ON users(email);
GO

-- ========================================
-- 插入测试数据
-- ========================================
INSERT INTO users (username, password, email) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com');
GO

-- ========================================
-- 创建视图
-- ========================================
CREATE VIEW v_user_info AS
SELECT 
    id,
    username,
    email,
    created_at,
    updated_at
FROM users;
GO

-- ========================================
-- 创建存储过程 - 用户统计
-- ========================================
CREATE PROCEDURE sp_user_statistics
AS
BEGIN
    SELECT 
        COUNT(*) AS total_users,
        SUM(CASE WHEN CAST(created_at AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS today_new_users,
        SUM(CASE WHEN created_at >= DATEADD(day, -7, GETDATE()) THEN 1 ELSE 0 END) AS week_new_users,
        SUM(CASE WHEN created_at >= DATEADD(month, -1, GETDATE()) THEN 1 ELSE 0 END) AS month_new_users
    FROM users;
END;
GO

-- ========================================
-- 创建触发器 - 自动更新时间戳
-- ========================================
CREATE TRIGGER tr_user_update_timestamp
ON users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE u
    SET updated_at = GETDATE()
    FROM users u
    INNER JOIN inserted i ON u.id = i.id;
END;
GO

-- ========================================
-- 查询测试数据
-- ========================================
SELECT TOP 10 * FROM v_user_info;
GO

-- 执行统计存储过程
EXEC sp_user_statistics;
GO

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库部署完成!' AS message;
GO
