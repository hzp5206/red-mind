# 本地启动说明

## 1. 环境要求

- JDK 8+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 5.0+

## 2. 初始化数据库

创建数据库：

```sql
CREATE DATABASE red_mind DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

初始化方式：

- 现在已经接入 `Flyway`
- 首次只需要创建空数据库 `red_mind`
- 启动后端时会自动执行 `backend/src/main/resources/db/migration` 下的增量脚本

## 3. 配置说明

编辑 `backend/src/main/resources/application.yml`：

- 修改 MySQL 用户名密码
- 修改 Redis 地址
- 生产环境替换 `redmind.jwt.secret`
- 接入真实模型时替换 `redmind.ai.base-url` 与 `redmind.ai.api-key`

## 4. 数据库迁移机制

- 迁移工具：`Flyway`
- 迁移目录：`backend/src/main/resources/db/migration`
- 命名规范：`V版本号__描述.sql`
- 以后新增表、加字段、补初始化数据，都新增一个迁移文件
- 你不需要反复手工执行全量 SQL，重启后端即可自动执行未跑过的增量脚本

## 5. 启动后端

```bash
cd backend
mvn spring-boot:run
```

接口文档：

- `http://localhost:8080/doc.html`

## 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问地址：

- `http://localhost:5173`

## 7. 当前默认实现

- AI 生成器默认使用 `MockAiProvider`
- 可通过 `redmind.ai.provider=openai` 切换到真实模型
- SSE 已打通，可用于前端流式联调
- 内容净化走本地敏感词词库
- 已支持模板回填、历史筛选、文案优化和剩余次数展示
- 会员支付、微信登录、对象存储仍需二期接入

## 8. 首次迁移说明

- 如果你当前数据库已经手工建过表，`Flyway` 会基于 `baseline-on-migrate: true` 建立基线
- 对于已存在数据的老库，建议先备份
- 对于全新环境，直接创建空库再启动服务即可

## 9. 二开建议

- 新增 `OpenAiProvider` 与模型路由策略
- 将 Prompt 模板配置化到数据库
- 将历史详情 `results` 在前端改为结构化渲染
- 将敏感词后台管理持久化到数据库，避免重启后丢失内存修改
- 生产环境建议为管理后台接口补充角色权限控制
- 初始化管理员账号时，请将 `users.role` 设置为 `ADMIN`
