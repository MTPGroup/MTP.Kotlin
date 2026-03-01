# Azusa Server

MomoTalk Plus 后端服务，基于 **Kotlin + Ktor**，采用 **DDD + 六边形架构**。

## 功能

- 认证（邮箱/OTP/JWT）
- 角色（Character）
- 聊天（SSE 流式）
- 知识库（文件 + 向量搜索）
- 插件系统
- 用户设置与主题

## 快速开始

```bash
cp .env.example .env
# 可选：启动依赖（PostgreSQL/Redis/S3/邮件测试）
docker-compose up -d
# 启动服务
./gradlew :server:run
```

## 常用命令

```bash
./gradlew :server:test
./gradlew :server:build
./gradlew :server:buildFatJar
./gradlew :server:buildImage
```

## 文档

- Swagger UI: `http://localhost:8080/swaggerUI`

## 约定

- 统一响应：`ApiResponse`
- 统一字段校验：`ValidationCollector`（失败返回 `400`）
- 数据库迁移：Flyway（启动自动执行）

## License

[GNU GPL v3.0](../LICENSE)
