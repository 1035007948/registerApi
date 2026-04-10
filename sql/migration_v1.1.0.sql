-- ========================================
-- 数据库迁移脚本示例
-- 版本: v1.0.0 -> v1.1.0
-- 说明: 演示如何进行数据库结构升级
-- ========================================

-- 迁移前备份提示
SELECT '开始迁移前，请确保已备份数据库!' AS warning;

-- ========================================
-- 版本控制表
-- ========================================
CREATE TABLE IF NOT EXISTS schema_version (
    version VARCHAR(20) PRIMARY KEY,
    description VARCHAR(255),
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT TRUE
);

-- 记录当前版本
INSERT INTO schema_version (version, description) 
VALUES ('1.0.0', '初始版本') 
ON DUPLICATE KEY UPDATE version='1.0.0';

-- ========================================
-- 迁移脚本 v1.1.0
-- 功能: 添加用户状态和角色字段
-- ========================================

-- 1. 添加用户状态字段
ALTER TABLE users 
ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '用户状态: ACTIVE, INACTIVE, LOCKED' AFTER email;

-- 2. 添加角色字段
ALTER TABLE users 
ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER, ADMIN, MODERATOR' AFTER status;

-- 3. 添加最后登录时间
ALTER TABLE users 
ADD COLUMN last_login_at TIMESTAMP NULL COMMENT '最后登录时间' AFTER updated_at;

-- 4. 添加登录失败次数
ALTER TABLE users 
ADD COLUMN login_attempts INT DEFAULT 0 COMMENT '登录失败次数' AFTER last_login_at;

-- 5. 添加手机号字段
ALTER TABLE users 
ADD COLUMN phone VARCHAR(20) NULL COMMENT '手机号' AFTER email;

-- 6. 为手机号添加唯一索引
ALTER TABLE users 
ADD UNIQUE INDEX uk_phone (phone);

-- 7. 创建登录日志表
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    login_status VARCHAR(20) COMMENT '登录状态: SUCCESS, FAILED',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 8. 创建用户操作日志表
CREATE TABLE IF NOT EXISTS user_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_type VARCHAR(50) NOT NULL COMMENT '活动类型: LOGIN, LOGOUT, UPDATE, DELETE',
    description TEXT COMMENT '活动描述',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动日志表';

-- 9. 更新现有用户数据
UPDATE users 
SET status = 'ACTIVE', role = 'USER' 
WHERE status IS NULL OR role IS NULL;

-- 10. 创建管理员用户
INSERT INTO users (username, password, email, role, status) 
VALUES ('superadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE role='ADMIN';

-- 11. 创建索引优化查询
CREATE INDEX idx_users_status_role ON users(status, role);

-- 12. 更新存储过程
DELIMITER //

DROP PROCEDURE IF EXISTS sp_user_statistics//

CREATE PROCEDURE sp_user_statistics()
BEGIN
    SELECT 
        COUNT(*) AS total_users,
        COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) AS active_users,
        COUNT(CASE WHEN status = 'INACTIVE' THEN 1 END) AS inactive_users,
        COUNT(CASE WHEN status = 'LOCKED' THEN 1 END) AS locked_users,
        COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) AS admin_users,
        COUNT(CASE WHEN role = 'USER' THEN 1 END) AS normal_users,
        COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) AS today_new_users,
        COUNT(CASE WHEN created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN 1 END) AS week_new_users,
        COUNT(CASE WHEN created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) AS month_new_users
    FROM users;
END //

DELIMITER ;

-- 13. 创建触发器记录用户活动
DELIMITER //

DROP TRIGGER IF EXISTS tr_user_activity_insert//

CREATE TRIGGER tr_user_activity_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO user_activity_logs (user_id, activity_type, description)
    VALUES (NEW.id, 'CREATE', CONCAT('用户创建: ', NEW.username));
END //

DROP TRIGGER IF EXISTS tr_user_activity_update//

CREATE TRIGGER tr_user_activity_update
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.email != OLD.email THEN
        INSERT INTO user_activity_logs (user_id, activity_type, description)
        VALUES (NEW.id, 'UPDATE', CONCAT('邮箱更新: ', OLD.email, ' -> ', NEW.email));
    END IF;
    
    IF NEW.status != OLD.status THEN
        INSERT INTO user_activity_logs (user_id, activity_type, description)
        VALUES (NEW.id, 'UPDATE', CONCAT('状态更新: ', OLD.status, ' -> ', NEW.status));
    END IF;
END //

DROP TRIGGER IF EXISTS tr_user_activity_delete//

CREATE TRIGGER tr_user_activity_delete
BEFORE DELETE ON users
FOR EACH ROW
BEGIN
    INSERT INTO user_activity_logs (user_id, activity_type, description)
    VALUES (OLD.id, 'DELETE', CONCAT('用户删除: ', OLD.username));
END //

DELIMITER ;

-- 14. 记录迁移版本
INSERT INTO schema_version (version, description) 
VALUES ('1.1.0', '添加用户状态、角色、登录日志等功能');

-- ========================================
-- 验证迁移结果
-- ========================================

-- 检查表结构
DESCRIBE users;
DESCRIBE login_logs;
DESCRIBE user_activity_logs;

-- 检查版本
SELECT * FROM schema_version;

-- 执行统计
CALL sp_user_statistics();

-- ========================================
-- 回滚脚本 (如果需要)
-- ========================================
/*
-- 警告: 执行回滚将丢失数据!

-- 删除新增字段
ALTER TABLE users 
DROP COLUMN status,
DROP COLUMN role,
DROP COLUMN last_login_at,
DROP COLUMN login_attempts,
DROP COLUMN phone;

-- 删除新增表
DROP TABLE IF EXISTS user_activity_logs;
DROP TABLE IF EXISTS login_logs;

-- 删除版本记录
DELETE FROM schema_version WHERE version = '1.1.0';

-- 恢复原存储过程
DELIMITER //

DROP PROCEDURE IF EXISTS sp_user_statistics//

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
*/

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库迁移完成! 版本: v1.1.0' AS message;
