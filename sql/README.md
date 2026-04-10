# SQL 部署脚本说明

## 概述
本目录包含用户注册系统的数据库部署脚本，支持多种主流数据库。

## 支持的数据库

### 1. MySQL (推荐生产环境)
- **文件**: `mysql_deploy.sql`
- **版本要求**: MySQL 5.7+
- **字符集**: UTF-8 (utf8mb4)
- **特性**:
  - 自动创建数据库
  - 完整的表结构和索引
  - 存储过程和触发器
  - 测试数据

**使用方法**:
```bash
# 方式1: 命令行执行
mysql -u root -p < mysql_deploy.sql

# 方式2: 登录MySQL后执行
mysql -u root -p
source /path/to/mysql_deploy.sql
```

### 2. PostgreSQL
- **文件**: `postgresql_deploy.sql`
- **版本要求**: PostgreSQL 9.6+
- **特性**:
  - 使用SERIAL类型
  - 支持JSON查询
  - 函数和触发器

**使用方法**:
```bash
# 方式1: 命令行执行
psql -U postgres -f postgresql_deploy.sql

# 方式2: 登录PostgreSQL后执行
psql -U postgres
\i /path/to/postgresql_deploy.sql
```

### 3. Oracle
- **文件**: `oracle_deploy.sql`
- **版本要求**: Oracle 11g+
- **特性**:
  - 使用IDENTITY列
  - PL/SQL存储过程
  - 完整的事务支持

**使用方法**:
```bash
# 使用SQL*Plus
sqlplus username/password@database @oracle_deploy.sql
```

### 4. SQL Server
- **文件**: `sqlserver_deploy.sql`
- **版本要求**: SQL Server 2016+
- **特性**:
  - 使用IDENTITY
  - T-SQL存储过程
  - 扩展属性注释

**使用方法**:
```bash
# 使用sqlcmd
sqlcmd -S server_name -U username -P password -i sqlserver_deploy.sql
```

## 数据库表结构

### users 表
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 用户ID | PRIMARY KEY, AUTO_INCREMENT |
| username | VARCHAR(50) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(255) | 密码(BCrypt加密) | NOT NULL |
| email | VARCHAR(100) | 邮箱地址 | NOT NULL, UNIQUE |
| created_at | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

## 索引设计

### 主键索引
- `PRIMARY KEY (id)`

### 唯一索引
- `UNIQUE INDEX (username)`
- `UNIQUE INDEX (email)`

### 普通索引
- `INDEX (created_at)` - 用于按时间查询
- `INDEX (email)` - 用于邮箱查询优化

## 生产环境配置建议

### 1. MySQL 配置优化
```ini
[mysqld]
# 字符集
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# InnoDB配置
innodb_buffer_pool_size=1G
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=2

# 连接配置
max_connections=200
max_connect_errors=1000
```

### 2. 数据库用户权限
```sql
-- 创建应用专用用户
CREATE USER 'register_app'@'%' IDENTIFIED BY 'secure_password_here';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON register_db.* TO 'register_app'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 3. 安全建议
- 使用强密码
- 限制数据库用户权限
- 启用SSL连接
- 定期备份数据
- 监控慢查询日志

## 应用配置

### Spring Boot 配置 (application-prod.properties)
```properties
# MySQL 配置
spring.datasource.url=jdbc:mysql://localhost:3306/register_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=register_app
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## 数据迁移

### 从H2迁移到MySQL
```bash
# 1. 导出H2数据
# 2. 转换SQL语法
# 3. 导入MySQL
mysql -u root -p register_db < migrated_data.sql
```

## 备份策略

### MySQL 备份
```bash
# 全量备份
mysqldump -u root -p register_db > backup_$(date +%Y%m%d).sql

# 恢复备份
mysql -u root -p register_db < backup_20260410.sql
```

## 性能监控

### 查看表状态
```sql
-- MySQL
SHOW TABLE STATUS LIKE 'users';

-- 查看索引使用情况
SHOW INDEX FROM users;

-- 查看表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'register_db';
```

## 故障排查

### 常见问题

1. **字符集问题**
   - 确保数据库、表、连接都使用UTF-8
   - MySQL使用utf8mb4而不是utf8

2. **连接超时**
   - 检查防火墙设置
   - 调整连接池参数

3. **性能问题**
   - 检查索引是否正确创建
   - 使用EXPLAIN分析慢查询

## 测试数据

所有脚本都包含测试数据，默认密码为 `password123`（BCrypt加密）。

**重要**: 生产环境部署前请删除测试数据！

```sql
-- 删除测试数据
DELETE FROM users WHERE username IN ('admin', 'testuser');
```

## 版本历史

- v1.0.0 (2026-04-10)
  - 初始版本
  - 支持MySQL、PostgreSQL、Oracle、SQL Server
  - 完整的表结构和索引
  - 存储过程和触发器
