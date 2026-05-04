# MyChatMind - AI Agent 系统

一个基于 Spring Boot + Spring AI 的智能 Agent 系统，支持多模型、工具调用、RAG 知识库检索。

## 项目概述

MyChatMind 是对 JChatMind 项目的后端复刻，实现了一个完整的 AI Agent 系统：

- **Agent Loop**: Think-Execute 循环，多轮规划与工具调用
- **多模型支持**: DeepSeek、智谱 AI (glm-4.6)、无问芯穹 (kimi-k2.5)
- **工具框架**: 固定工具 + 可选工具，可扩展
- **RAG 知识库**: Markdown 文档解析、向量嵌入、相似度检索
- **SSE 实时推送**: Agent 执行状态实时展示

## 技术栈

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.5.x |
| Spring AI | 1.1.0 |
| PostgreSQL + pgvector | 16 + 0.7 |
| MyBatis | 3.0.3 |
| React | 19.2.0 |
| Ant Design | 6.0.0 |
| Vite | 7.x |

## 项目结构

```
com.study.mychatmind/
├── agent/
│   ├── AgentState.java           # Agent 状态枚举
│   ├── ChatAgentAFactory.java    # Agent 工厂
│   ├── MyChatMindAgent.java      # Agent 核心 (Think-Execute)
│   └── tools/
│       ├── Tool.java             # 工具接口
│       ├── ToolType.java         # 工具类型 (FIXED/OPTIONAL)
│       ├── TerminateTool.java    # 终止工具
│       ├── DirectAnswerTool.java # 直接回答工具
│       ├── DatabaseQueryTool.java# 数据库查询工具
│       ├── EmailTool.java        # 邮件发送工具
│       └── KnowledgeTool.java    # 知识库检索工具
├── config/
│   ├── ChatModelRegistry.java    # 模型注册表
│   ├── MultiChatModelConfig.java # 多模型配置
│   └── CorsConfig.java           # CORS 配置
├── controller/
│   ├── AgentController.java      # Agent CRUD
│   ├── AgentRunController.java   # Agent 运行
│   ├── SseController.java        # SSE 连接
│   └── ...
├── service/
│   ├── SseService.java           # SSE 推送
│   ├── RagService.java           # RAG 服务
│   ├── MarkdownParserService.java# Markdown 解析
│   └── ...
├── message/
│   └── SseMessage.java           # SSE 消息结构
└── ...
```

## 核心功能

### 1. Agent Loop (Think-Execute)

```
用户输入 → Think (AI决策) → Execute (工具调用) → Think → ... → 完成
```

- 手动控制工具执行（关闭 Spring AI 自动执行）
- 最大 20 步循环
- 消息持久化到数据库
- SSE 实时推送执行状态

### 2. 多模型架构

| 模型 | Bean 名称 | Provider |
|------|-----------|----------|
| DeepSeek | deepseek-chat | Spring AI DeepSeek |
| 智谱 AI | glm-4.6 | Spring AI ZhipuAI |
| 无问芯穹 | infini-ai | OpenAI 兼容接口 |

通过 `ChatModelRegistry` 注册表模式，运行时动态切换模型。

### 3. 工具框架

| 工具 | 类型 | 功能 |
|------|------|------|
| TerminateTool | FIXED | 终止 Agent 循环 |
| DirectAnswerTool | FIXED | 直接回答用户问题 |
| KnowledgeTool | FIXED | 知识库检索 |
| DatabaseQueryTool | OPTIONAL | 数据库查询 |
| EmailTool | OPTIONAL | 发送邮件 |

工具通过 `@Component` 自动注入，分为 FIXED（所有 Agent 都有）和 OPTIONAL（按配置绑定）。

### 4. RAG 知识库

```
上传 Markdown → 解析分块 → Embedding → 存入 pgvector
查询 → Embedding → 相似度检索 → 返回 top 3
```

- 使用 Ollama BGE-M3 本地嵌入模型 (1024 维)
- pgvector cosine 距离检索
- IVFFlat 索引加速

### 5. SSE 实时推送

| 状态 | 说明 |
|------|------|
| AI_THINKING | 正在思考 |
| AI_EXECUTING | 正在执行工具 |
| AI_GENERATED_CONTENT | AI 回复内容 |
| AI_DONE | 任务完成 |

## 快速开始

### 1. 环境准备

- JDK 17+
- PostgreSQL + pgvector (Docker)
- Ollama (可选，用于 RAG)

### 2. 启动数据库

```bash
docker run -d \
  --name pgvector \
  -e POSTGRES_PASSWORD=123456 \
  -p 5433:5432 \
  pgvector/pgvector:pg16

# 创建数据库
docker exec -it pgvector psql -U postgres -c "CREATE DATABASE jchatmind;"
docker exec -it pgvector psql -U postgres -d jchatmind -c "CREATE EXTENSION vector;"
```

### 3. 启动后端

```bash
cd MyChatMind
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd ui
npm install
npm run dev
```

### 5. 访问应用

- 前端: http://localhost:5173
- 后端 API: http://localhost:8080/api

## API 接口

### Agent

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/agents` | GET | 获取所有 Agent |
| `/api/agents` | POST | 创建 Agent |
| `/api/agents/{id}` | DELETE | 删除 Agent |
| `/api/agent/{id}/session/{sid}/run` | POST | 运行 Agent |

### SSE

| 接口 | 方法 | 说明 |
|------|------|------|
| `/sse/connect/{sessionId}` | GET | SSE 连接 |

### 知识库

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/knowledge-bases` | GET | 获取所有知识库 |
| `/api/knowledge-bases` | POST | 创建知识库 |
| `/api/documents/upload` | POST | 上传文档 |

## 配置说明

### application.yaml

```yaml
spring:
  ai:
    deepseek:
      api-key: your-api-key
    zhipuai:
      api-key: your-api-key
    openai:
      api-key: your-api-key
      base-url: https://cloud.infini-ai.com/maas/coding

ollama:
  base-url: http://localhost:11434
  embedding-model: bge-m3

document:
  storage:
    base-path: ./data/documents
```

## 开发进度

- [x] Week 1: 基础架构 + CRUD
- [x] Week 2: 多模型 + Agent Loop
- [x] Week 3: 工具框架 + RAG
- [x] Week 4: SSE + 前后端联调

## 项目亮点

1. **Think-Execute 循环**: 手动控制工具执行，实现多轮规划
2. **多模型架构**: 注册表模式，运行时动态切换模型
3. **可扩展工具框架**: 固定工具 + 可选工具，Spring 自动收集
4. **完整 RAG 链路**: Markdown 解析 + pgvector 检索
5. **SSE 实时推送**: Agent 执行过程可视化

## 相关项目

- 原项目: [JChatMind](../JChatMind)

## License

MIT