-- ========================================
-- 用户注册系统 - Oracle 数据库部署脚本
-- 版本: 1.0.0
-- 数据库: Oracle 11g+
-- ========================================

-- 创建表空间（根据实际需求调整）
-- CREATE TABLESPACE register_ts DATAFILE 'register_ts.dbf' SIZE 100M AUTOEXTEND ON;

-- ========================================
-- 创建用户表
-- ========================================
CREATE TABLE users (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL,
    email VARCHAR2(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT SYSTIMESTAMP
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
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_email ON users(email);

-- ========================================
-- 创建序列（如果使用Oracle 11g）
-- ========================================
-- CREATE SEQUENCE seq_users_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ========================================
-- 插入测试数据
-- ========================================
INSERT INTO users (username, password, email) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com');

INSERT INTO users (username, password, email) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com');

COMMIT;

-- ========================================
-- 创建触发器 - 自动更新时间戳
-- ========================================
CREATE OR REPLACE TRIGGER tr_user_update_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- ========================================
-- 创建存储过程 - 用户统计
-- ========================================
CREATE OR REPLACE PROCEDURE sp_user_statistics (
    p_total_users OUT NUMBER,
    p_today_new_users OUT NUMBER,
    p_week_new_users OUT NUMBER,
    p_month_new_users OUT NUMBER
) AS
BEGIN
    SELECT COUNT(*) INTO p_total_users FROM users;
    
    SELECT COUNT(*) INTO p_today_new_users 
    FROM users 
    WHERE TRUNC(created_at) = TRUNC(SYSDATE);
    
    SELECT COUNT(*) INTO p_week_new_users 
    FROM users 
    WHERE created_at >= SYSDATE - 7;
    
    SELECT COUNT(*) INTO p_month_new_users 
    FROM users 
    WHERE created_at >= ADD_MONTHS(SYSDATE, -1);
END;
/

-- ========================================
-- 查询测试数据
-- ========================================
SELECT * FROM users WHERE ROWNUM <= 10;

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库部署完成!' AS message FROM dual;
