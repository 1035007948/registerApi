# 注册接口 API 文档

## 项目概述
这是一个基于 Spring Boot 2.7.18 和 JDK 1.8 构建的用户注册管理系统，提供完整的 RESTful API 接口。

## 技术栈
- **Java**: JDK 1.8
- **Spring Boot**: 2.7.18
- **数据库**: H2 内存数据库
- **认证**: JWT (JSON Web Token)
- **密码加密**: BCrypt
- **构建工具**: Maven

## 项目结构
```
src/main/java/com/example/registerapi/
├── config/              # 配置类
│   └── SecurityConfig.java
├── controller/          # 控制器层
│   └── UserController.java
├── dto/                 # 数据传输对象
│   ├── JwtResponse.java
│   ├── LoginDto.java
│   ├── UserRegistrationDto.java
│   └── UserUpdateDto.java
├── exception/           # 异常处理
│   └── GlobalExceptionHandler.java
├── model/               # 实体类
│   └── User.java
├── repository/          # 数据访问层
│   └── UserRepository.java
├── security/            # 安全相关
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenUtil.java
├── service/             # 业务逻辑层
│   ├── AuthService.java
│   └── UserService.java
└── RegisterApiApplication.java
```

## API 接口说明

### 1. 用户注册
**POST** `/api/register`

请求体:
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

响应示例:
```json
{
  "message": "用户注册成功",
  "userId": 1,
  "username": "testuser"
}
```

### 2. 用户登录
**POST** `/api/auth/login`

请求体:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

响应示例:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "id": 1
}
```

### 3. 获取所有用户
**GET** `/api/users`

请求头:
```
Authorization: Bearer <token>
```

响应示例:
```json
[
  {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "createdAt": "2026-04-10T11:20:00",
    "updatedAt": "2026-04-10T11:20:00"
  }
]
```

### 4. 获取当前用户信息
**GET** `/api/users/me`

请求头:
```
Authorization: Bearer <token>
```

### 5. 根据ID获取用户
**GET** `/api/users/{id}`

请求头:
```
Authorization: Bearer <token>
```

### 6. 更新用户信息
**PUT** `/api/users/{id}`

请求头:
```
Authorization: Bearer <token>
```

请求体:
```json
{
  "email": "newemail@example.com"
}
```

### 7. 删除用户
**DELETE** `/api/users/{id}`

请求头:
```
Authorization: Bearer <token>
```

## 运行项目

### 1. 编译项目
```bash
mvn clean compile
```

### 2. 运行测试
```bash
mvn test
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 4. 访问 H2 控制台
访问 `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- 用户名: `sa`
- 密码: (留空)

## 功能特性

### ✅ 已实现功能
1. **CRUD 操作**
   - 用户注册（Create）
   - 查询用户（Read）
   - 更新用户信息（Update）
   - 删除用户（Delete）

2. **认证功能**
   - JWT Token 认证
   - 密码 BCrypt 加密
   - 登录验证

3. **中间件**
   - JWT 认证过滤器
   - 全局异常处理
   - 请求验证

4. **数据库交互**
   - Spring Data JPA
   - H2 内存数据库
   - 自动建表

5. **测试**
   - 单元测试
   - 集成测试
   - 测试覆盖率

## 验证规则
- 用户名：3-50个字符，不能为空，唯一
- 密码：至少6个字符，不能为空
- 邮箱：有效邮箱格式，不能为空，唯一

## 安全配置
- 所有接口（除注册和登录）都需要 JWT 认证
- 密码使用 BCrypt 加密存储
- JWT Token 有效期为 24 小时（86400000 毫秒）

## 测试结果
所有 17 个测试用例全部通过 ✅
- RegisterApiApplicationTests: 1 个测试
- UserControllerIntegrationTest: 7 个测试
- UserServiceTest: 9 个测试
