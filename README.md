# Red Mind

小红书文档自动生成系统的 PC 端管理后台与后端服务脚手架。

## 项目结构

- `backend`：Spring Boot 2.7 + MyBatis-Plus + Redis + JWT + Knife4j
- `frontend`：React + TypeScript + Vite + Ant Design 管理端
- `docs`：架构设计与后续迭代说明

## MVP 范围

- 登录/注册/JWT 鉴权
- 文案生成页（含 SSE 流式生成）
- 内容净化
- 历史记录
- 灵感库 / 收藏
- 会员与次数控制基础能力
- 敏感词过滤 AC 自动机

## 快速开始

### 后端

```bash
cd backend
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

## 默认约定

- 后端端口：`8080`
- 前端端口：`5173`
- API 前缀：`/api/v1`
- 数据库：MySQL 8
- 缓存：Redis 5+

## 当前实现说明

当前版本优先交付完整业务骨架、关键领域模型、接口契约、前后端页面闭环与可扩展代码组织，便于后续接入真实 AI 服务、支付与对象存储。
