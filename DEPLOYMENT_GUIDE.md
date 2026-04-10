# 生产环境部署指南

## 目录
1. [环境准备](#环境准备)
2. [数据库部署](#数据库部署)
3. [应用配置](#应用配置)
4. [应用部署](#应用部署)
5. [安全配置](#安全配置)
6. [监控和维护](#监控和维护)

## 环境准备

### 系统要求
- **操作系统**: Linux/Windows Server
- **Java**: JDK 1.8+
- **数据库**: MySQL 5.7+ / PostgreSQL 9.6+ / Oracle 11g+ / SQL Server 2016+
- **内存**: 最小 512MB，推荐 2GB+
- **磁盘**: 最小 1GB

### 必需软件
```bash
# Java
java -version

# Maven (用于构建)
mvn -version

# 数据库客户端
mysql --version  # 或 psql --version
```

## 数据库部署

### MySQL 部署 (推荐)

#### 1. 安装 MySQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# Windows: 下载安装包
# https://dev.mysql.com/downloads/mysql/
```

#### 2. 执行部署脚本
```bash
# 登录MySQL
mysql -u root -p

# 执行部署脚本
source /path/to/sql/mysql_deploy.sql

# 或直接执行
mysql -u root -p < sql/mysql_deploy.sql
```

#### 3. 创建应用用户
```sql
-- 创建专用用户
CREATE USER 'register_app'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON register_db.* TO 'register_app'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证权限
SHOW GRANTS FOR 'register_app'@'localhost';
```

#### 4. 配置MySQL
编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`:
```ini
[mysqld]
# 字符集
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# 性能优化
innodb_buffer_pool_size=1G
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=2

# 连接配置
max_connections=200
max_connect_errors=1000

# 慢查询日志
slow_query_log=1
slow_query_log_file=/var/log/mysql/slow.log
long_query_time=2
```

重启MySQL:
```bash
sudo systemctl restart mysql
```

### PostgreSQL 部署

#### 1. 安装 PostgreSQL
```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql-server

# 初始化数据库
sudo postgresql-setup initdb
sudo systemctl start postgresql
```

#### 2. 执行部署脚本
```bash
# 切换到postgres用户
sudo -u postgres psql

# 执行脚本
\i /path/to/sql/postgresql_deploy.sql
```

#### 3. 创建应用用户
```sql
-- 创建用户
CREATE USER register_app WITH PASSWORD 'YourStrongPassword123!';

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE register_db TO register_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO register_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO register_app;
```

### Oracle 部署

#### 1. 安装 Oracle Database
参考 Oracle 官方文档安装 Oracle Database

#### 2. 执行部署脚本
```bash
# 使用 SQL*Plus
sqlplus system/password@localhost:1521/ORCL @sql/oracle_deploy.sql
```

#### 3. 创建应用用户
```sql
-- 创建用户
CREATE USER register_app IDENTIFIED BY "YourStrongPassword123!";

-- 授予权限
GRANT CONNECT, RESOURCE TO register_app;
GRANT CREATE SESSION TO register_app;
GRANT CREATE TABLE TO register_app;
GRANT CREATE SEQUENCE TO register_app;
```

### SQL Server 部署

#### 1. 安装 SQL Server
```bash
# Ubuntu
curl https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add -
curl https://packages.microsoft.com/config/ubuntu/20.04/mssql-server-2019.list | sudo tee /etc/apt/sources.list.d/mssql-server.list
sudo apt update
sudo apt install mssql-server

# 配置
sudo /opt/mssql/bin/mssql-conf setup
```

#### 2. 执行部署脚本
```bash
# 使用 sqlcmd
sqlcmd -S localhost -U sa -P your_password -i sql/sqlserver_deploy.sql
```

## 应用配置

### 1. 选择配置文件
根据使用的数据库选择对应的配置文件：

- **MySQL**: `application-prod.properties`
- **PostgreSQL**: `application-postgresql.properties`
- **Oracle**: `application-oracle.properties`
- **SQL Server**: `application-sqlserver.properties`

### 2. 修改配置文件
编辑选定的配置文件，更新以下关键配置：

```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://your-db-host:3306/register_db
spring.datasource.username=register_app
spring.datasource.password=YourStrongPassword123!

# JWT密钥 (必须修改!)
jwt.secret=your_production_secret_key_at_least_256_bits_long_for_security

# 连接池大小 (根据实际情况调整)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### 3. 生成安全的JWT密钥
```bash
# 使用OpenSSL生成256位密钥
openssl rand -base64 32

# 或使用Java
java -e "import java.util.UUID; System.out.println(UUID.randomUUID().toString().replace(\"-\", \"\") + UUID.randomUUID().toString().replace(\"-\", \"\"));"
```

## 应用部署

### 1. 构建应用
```bash
# 清理并编译
mvn clean package -DskipTests

# 或包含测试
mvn clean package
```

### 2. 运行应用

#### 方式1: 使用JAR包
```bash
# 使用生产配置
java -jar target/register-api-1.0.0.jar --spring.profiles.active=prod

# 或指定配置文件
java -jar target/register-api-1.0.0.jar --spring.config.location=classpath:/application-prod.properties
```

#### 方式2: 使用Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### 方式3: 作为系统服务 (Linux)

创建服务文件 `/etc/systemd/system/register-api.service`:
```ini
[Unit]
Description=Register API Service
After=network.target mysql.service

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/register-api
ExecStart=/usr/bin/java -jar /opt/register-api/register-api-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl daemon-reload
sudo systemctl start register-api
sudo systemctl enable register-api
sudo systemctl status register-api
```

### 3. 验证部署
```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 测试注册接口
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## 安全配置

### 1. 防火墙配置
```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 8080/tcp
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 2. HTTPS 配置 (推荐)

#### 使用 Let's Encrypt
```bash
# 安装 Certbot
sudo apt install certbot

# 获取证书
sudo certbot certonly --standalone -d yourdomain.com

# 配置Spring Boot
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=/etc/letsencrypt/live/yourdomain.com/keystore.p12
server.ssl.key-store-password=your_password
server.ssl.key-store-type=PKCS12
```

#### 使用 Nginx 反向代理
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. 数据库安全
```sql
-- 删除测试数据
DELETE FROM users WHERE username IN ('admin', 'testuser');

-- 限制远程访问
-- MySQL
GRANT ALL PRIVILEGES ON register_db.* TO 'register_app'@'localhost';

-- 禁用root远程登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
FLUSH PRIVILEGES;
```

### 4. 应用安全
```properties
# 禁用敏感端点
management.endpoints.web.exposure.include=health,info

# 安全headers
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

## 监控和维护

### 1. 日志管理
```bash
# 查看应用日志
tail -f /var/log/register-api/application.log

# 日志轮转配置 /etc/logrotate.d/register-api
/var/log/register-api/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 appuser appuser
}
```

### 2. 性能监控
```bash
# JVM监控
jstat -gc <pid> 1000

# 线程dump
jstack <pid> > thread_dump.txt

# 堆dump
jmap -dump:format=b,file=heap_dump.hprof <pid>
```

### 3. 数据库备份

#### MySQL 自动备份脚本
```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/register_db_$DATE.sql.gz"

mkdir -p $BACKUP_DIR

mysqldump -u register_app -p'YourStrongPassword123!' register_db | gzip > $BACKUP_FILE

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: $BACKUP_FILE"
```

添加到crontab:
```bash
# 每天凌晨2点备份
0 2 * * * /opt/scripts/backup.sh >> /var/log/backup.log 2>&1
```

### 4. 健康检查
```bash
# 创建健康检查脚本
#!/bin/bash
# health_check.sh

RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

if [ $RESPONSE -eq 200 ]; then
    echo "Application is healthy"
    exit 0
else
    echo "Application is unhealthy: HTTP $RESPONSE"
    # 发送告警邮件或通知
    exit 1
fi
```

### 5. 性能优化建议

#### JVM参数优化
```bash
java -jar register-api-1.0.0.jar \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/register-api/heap_dump.hprof \
  --spring.profiles.active=prod
```

#### 数据库优化
```sql
-- 定期优化表
OPTIMIZE TABLE users;

-- 分析表
ANALYZE TABLE users;

-- 检查表
CHECK TABLE users;
```

## 故障排查

### 常见问题

#### 1. 无法连接数据库
```bash
# 检查数据库服务
sudo systemctl status mysql

# 检查端口
netstat -tlnp | grep 3306

# 测试连接
mysql -h localhost -u register_app -p register_db
```

#### 2. 应用启动失败
```bash
# 查看详细日志
tail -f /var/log/register-api/application.log

# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java版本
java -version
```

#### 3. 性能问题
```bash
# 查看系统资源
top
free -m
df -h

# 查看数据库慢查询
tail -f /var/log/mysql/slow.log
```

## 升级和回滚

### 应用升级
```bash
# 1. 备份当前版本
cp register-api-1.0.0.jar register-api-1.0.0.jar.backup

# 2. 部署新版本
cp target/register-api-1.1.0.jar /opt/register-api/

# 3. 重启服务
sudo systemctl restart register-api

# 4. 验证
curl http://localhost:8080/actuator/health
```

### 数据库迁移
```bash
# 1. 备份数据库
mysqldump -u register_app -p register_db > backup_before_migration.sql

# 2. 执行迁移脚本
mysql -u register_app -p register_db < migration_v1.1.sql

# 3. 验证数据
mysql -u register_app -p register_db -e "SELECT COUNT(*) FROM users;"
```

## 联系支持

如遇到问题，请检查：
1. 应用日志: `/var/log/register-api/application.log`
2. 数据库日志: `/var/log/mysql/error.log`
3. 系统日志: `/var/log/syslog`

技术支持: support@example.com
