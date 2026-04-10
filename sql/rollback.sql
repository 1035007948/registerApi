-- ========================================================
-- 数据库回滚脚本
-- 用于清理和删除所有相关表和数据
-- ========================================================

USE register_db;

-- 删除触发器
DROP TRIGGER IF EXISTS trg_users_before_update;

-- 删除表
DROP TABLE IF EXISTS users;

-- 可选: 删除整个数据库 (谨慎使用)
-- DROP DATABASE IF EXISTS register_db;

SELECT '数据库回滚完成' AS message;
