# 系统架构设计

## 1. 总体方案

系统采用前后端分离架构：

- 前端：PC Web 管理端，承担创作流程、历史管理、灵感收藏与设置中心
- 后端：REST API + SSE 流式生成服务
- 基础设施：MySQL、Redis、敏感词词库、可插拔 AI Provider

## 2. 后端模块划分

- `auth`：注册、登录、JWT、游客能力识别
- `user`：用户信息、会员状态、额度控制
- `generate`：Prompt 组装、AI 调用、结果清洗、评分、历史入库
- `content`：敏感词扫描、净化替换
- `history`：生成记录查询、删除、收藏
- `template`：系统模板库
- `common`：统一响应、异常处理、工具类

## 3. 前端页面划分

- `/login`：登录/注册
- `/create`：文案生成主工作台
- `/library`：模板库 + 我的收藏
- `/history`：历史记录
- `/settings/billing`：会员方案与说明
- `/admin/templates`：模板管理
- `/admin/sensitive-words`：敏感词管理
- `/admin/users`：用户概览
- `/admin/dashboard`：后台首页

## 4. 关键设计

### 4.1 生成链路

1. 校验额度与入参
2. 构造风格化 Prompt
3. 生成 3 个版本
4. 进行内容后处理
5. 执行敏感词扫描与净化建议
6. 计算质量评分
7. 写入历史记录
8. 返回普通响应或 SSE 增量事件

### 4.2 可扩展 AI Provider

通过 `AiProvider` 接口解耦不同模型厂商：

- `MockAiProvider`：本地演示与联调
- `OpenAiProvider`：接入 OpenAI / 兼容 API
- `AiProviderRouter`：基于配置动态切换模型供应商

### 4.3 敏感词模块

- 基于 AC 自动机进行多模式匹配
- 支持违禁词与推荐替换词
- 支持启动时加载本地词库，后续可扩展后台维护

## 5. 下一步建议

- 接入真实支付与会员订单
- 增加图文混排编辑器
- 增加 Prompt 模板后台配置
- 增加运营看板与调用监控
