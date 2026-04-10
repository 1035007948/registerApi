# 数据库部署说明

## 文件说明

| 文件 | 说明 |
|------|------|
| `schema.sql` | 数据库结构定义和初始化数据 |
| `deploy.sh` | Linux/Mac 部署脚本 |
| `deploy.bat` | Windows 部署脚本 |
| `rollback.sql` | 数据库回滚脚本 |

## 快速部署

### 方式一: 使用脚本部署 (推荐)

**Windows:**
```bash
# 编辑 deploy.bat，修改数据库连接信息
# 然后双击运行
sql\deploy.bat
```

**Linux/Mac:**
```bash
# 编辑 deploy.sh，修改数据库连接信息
chmod +x sql/deploy.sh
./sql/deploy.sh
```

### 方式二: 手动执行SQL

```bash
# 登录MySQL
mysql -u root -p

# 执行脚本
source sql/schema.sql
```

## 数据库配置

### 修改 application.yml

如果使用MySQL数据库，修改 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/register_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: none  # 生产环境建议设为 none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

## 表结构

### users (用户表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | BCrypt加密密码 |
| email | VARCHAR(100) | 邮箱，唯一 |
| phone_number | VARCHAR(20) | 手机号 |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| status | VARCHAR(20) | 状态: ACTIVE/INACTIVE/LOCKED/DELETED |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| last_login_at | DATETIME | 最后登录时间 |

## 测试账号

| 用户名 | 密码 | 邮箱 | 状态 |
|--------|------|------|------|
| admin | 123456 | admin@example.com | 正常 |
| test | 123456 | test@example.com | 正常 |
| zhangsan | 123456 | zhangsan@example.com | 正常 |
| lisi | 123456 | lisi@example.com | 正常 |
| wangwu | 123456 | wangwu@example.com | 未激活 |

## 回滚操作

```bash
# 执行回滚脚本
mysql -u root -p < sql/rollback.sql
```

## 注意事项

1. 生产环境请修改默认密码
2. 建议定期备份数据库
3. 密码使用BCrypt加密，默认成本因子为10
