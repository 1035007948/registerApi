# SQL脚本快速参考

## 📁 文件列表

### 部署脚本
| 文件名 | 数据库 | 用途 | 使用场景 |
|--------|--------|------|----------|
| `mysql_deploy.sql` | MySQL 5.7+ | 完整部署脚本 | **生产环境推荐** |
| `postgresql_deploy.sql` | PostgreSQL 9.6+ | 完整部署脚本 | 开源环境 |
| `oracle_deploy.sql` | Oracle 11g+ | 完整部署脚本 | 企业环境 |
| `sqlserver_deploy.sql` | SQL Server 2016+ | 完整部署脚本 | Windows环境 |

### 迁移脚本
| 文件名 | 版本 | 说明 |
|--------|------|------|
| `migration_v1.1.0.sql` | v1.0.0 → v1.1.0 | 添加用户状态、角色、日志等功能 |

## 🚀 快速开始

### MySQL (推荐)
```bash
# 1. 部署数据库
mysql -u root -p < sql/mysql_deploy.sql

# 2. 创建应用用户
mysql -u root -p
CREATE USER 'register_app'@'localhost' IDENTIFIED BY 'YourPassword123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON register_db.* TO 'register_app'@'localhost';
FLUSH PRIVILEGES;

# 3. 配置应用
# 编辑 src/main/resources/application-prod.properties
# 更新数据库连接信息

# 4. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### PostgreSQL
```bash
# 1. 部署数据库
sudo -u postgres psql -f sql/postgresql_deploy.sql

# 2. 创建应用用户
sudo -u postgres psql
CREATE USER register_app WITH PASSWORD 'YourPassword123!';
GRANT ALL PRIVILEGES ON DATABASE register_db TO register_app;

# 3. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=postgresql
```

### Oracle
```bash
# 1. 部署数据库
sqlplus system/password@ORCL @sql/oracle_deploy.sql

# 2. 创建应用用户
sqlplus system/password@ORCL
CREATE USER register_app IDENTIFIED BY "YourPassword123!";
GRANT CONNECT, RESOURCE TO register_app;

# 3. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
```

### SQL Server
```bash
# 1. 部署数据库
sqlcmd -S localhost -U sa -P password -i sql/sqlserver_deploy.sql

# 2. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver
```

## 📊 数据库表结构

### 核心表: users
```sql
users (
    id            BIGINT       -- 用户ID (主键)
    username      VARCHAR(50)  -- 用户名 (唯一)
    password      VARCHAR(255) -- 密码 (BCrypt加密)
    email         VARCHAR(100) -- 邮箱 (唯一)
    created_at    DATETIME     -- 创建时间
    updated_at    DATETIME     -- 更新时间
)
```

### 扩展表 (v1.1.0)
```sql
login_logs (
    id            BIGINT       -- 日志ID
    user_id       BIGINT       -- 用户ID (外键)
    login_time    TIMESTAMP    -- 登录时间
    ip_address    VARCHAR(45)  -- IP地址
    login_status  VARCHAR(20)  -- 登录状态
)

user_activity_logs (
    id            BIGINT       -- 日志ID
    user_id       BIGINT       -- 用户ID (外键)
    activity_type VARCHAR(50)  -- 活动类型
    description   TEXT         -- 活动描述
    created_at    TIMESTAMP    -- 创建时间
)
```

## 🔧 常用操作

### 备份
```bash
# MySQL
mysqldump -u register_app -p register_db > backup_$(date +%Y%m%d).sql

# PostgreSQL
pg_dump -U register_app register_db > backup_$(date +%Y%m%d).sql

# Oracle
exp register_app/password@ORCL file=backup.dmp

# SQL Server
sqlcmd -S localhost -U sa -Q "BACKUP DATABASE register_db TO DISK='backup.bak'"
```

### 恢复
```bash
# MySQL
mysql -u register_app -p register_db < backup_20260410.sql

# PostgreSQL
psql -U register_app register_db < backup_20260410.sql

# Oracle
imp register_app/password@ORCL file=backup.dmp

# SQL Server
sqlcmd -S localhost -U sa -Q "RESTORE DATABASE register_db FROM DISK='backup.bak'"
```

### 查询统计
```sql
-- 用户统计
CALL sp_user_statistics();  -- MySQL
SELECT * FROM fn_user_statistics();  -- PostgreSQL
EXEC sp_user_statistics;  -- SQL Server

-- 查看用户列表
SELECT * FROM v_user_info LIMIT 10;

-- 查看表结构
DESCRIBE users;  -- MySQL
\d users  -- PostgreSQL
DESC users;  -- Oracle
sp_help users;  -- SQL Server
```

## 🔐 安全配置

### 数据库用户权限
```sql
-- 最小权限原则
GRANT SELECT, INSERT, UPDATE, DELETE ON register_db.* TO 'register_app'@'localhost';

-- 不要授予以下权限
-- DROP, ALTER, CREATE, INDEX, GRANT OPTION
```

### 密码策略
- 最小长度: 12位
- 包含: 大小写字母、数字、特殊字符
- 定期更换: 90天
- 避免使用: 常见单词、生日等

### 连接安全
```properties
# 启用SSL
spring.datasource.url=jdbc:mysql://host:3306/register_db?useSSL=true

# 加密传输
spring.datasource.hikari.data-source-properties.useSSL=true
```

## 📈 性能优化

### 索引优化
```sql
-- 查看索引使用情况
SHOW INDEX FROM users;  -- MySQL

-- 添加复合索引
CREATE INDEX idx_users_status_created ON users(status, created_at);

-- 分析查询性能
EXPLAIN SELECT * FROM users WHERE username = 'testuser';
```

### 查询优化
```sql
-- 避免SELECT *
SELECT id, username, email FROM users;

-- 使用LIMIT
SELECT * FROM users LIMIT 100;

-- 使用索引字段
SELECT * FROM users WHERE email = 'test@example.com';
```

### 连接池配置
```properties
# 推荐配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## 🐛 故障排查

### 连接失败
```bash
# 检查服务状态
sudo systemctl status mysql

# 检查端口
netstat -tlnp | grep 3306

# 测试连接
mysql -h localhost -u register_app -p register_db
```

### 性能问题
```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';

-- 查看进程
SHOW PROCESSLIST;

-- 杀死长时间运行的查询
KILL <process_id>;
```

### 锁等待
```sql
-- 查看锁
SHOW OPEN TABLES WHERE In_use > 0;

-- 查看事务
SELECT * FROM information_schema.INNODB_TRX;
```

## 📚 相关文档

- [API文档](API_DOCUMENTATION.md)
- [部署指南](DEPLOYMENT_GUIDE.md)
- [项目README](README.md)

## 💡 最佳实践

1. **备份策略**
   - 每日自动备份
   - 保留30天备份
   - 定期测试恢复

2. **监控告警**
   - 监控连接数
   - 监控慢查询
   - 监控磁盘空间

3. **版本管理**
   - 使用schema_version表
   - 记录每次迁移
   - 保留回滚脚本

4. **安全审计**
   - 记录所有操作
   - 定期审查权限
   - 监控异常登录

## 🆘 获取帮助

遇到问题时：
1. 查看应用日志
2. 查看数据库日志
3. 检查配置文件
4. 参考部署指南
5. 联系技术支持
