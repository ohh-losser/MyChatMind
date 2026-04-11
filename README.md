# MyChatMind

基于 Spring AI 的智能 Agent 助手系统后端，实现自主决策、工具调用和 RAG 知识库检索。

## 项目简介

MyChatMind 是一个 AI Agent（智能体）助手系统的后端服务，核心特性包括：

- **Agent Loop**：Think-Execute 循环，多轮规划与执行
- **工具调用框架**：可扩展的工具系统，支持数据库查询、邮件发送等
- **RAG 知识库**：基于 pgvector 的向量检索增强生成
- **多模型支持**：支持 DeepSeek、智谱 AI、OpenAI 兼容接口
- **SSE 实时通信**：实时推送 Agent 执行状态

## 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.x + Java 17 |
| AI 框架 | Spring AI 1.1.0 |
| 数据库 | PostgreSQL + pgvector |
| ORM | MyBatis 3.0.3 |
| 构建工具 | Maven |

## 项目结构

```
src/main/java/com/study/mychatmind/
├── agent/              # Agent 核心实现
│   └── tools/          # 工具类
├── config/             # 配置类
├── controller/         # REST API 控制器
├── service/            # 业务服务层
│   └── impl/           # 服务实现
├── mapper/             # MyBatis Mapper
├── model/
│   ├── entity/         # 实体类
│   ├── dto/            # 数据传输对象
│   ├── vo/             # 视图对象
│   └── request/        # 请求对象
├── converter/          # 对象转换器
├── exception/          # 异常处理
└── typehandler/        # MyBatis 类型处理器
```

## 数据库设计

核心表结构：

| 表名 | 说明 |
|------|------|
| `agent` | 智能体配置（角色、工具、知识库绑定） |
| `chat_session` | 会话管理 |
| `chat_message` | 消息记录（支持 user/assistant/tool 角色） |
| `knowledge_base` | 知识库 |
| `document` | 文档管理 |
| `chunk_bge_m3` | 文档分块与向量存储 |

## 快速开始

### 环境要求

- JDK 17+
- PostgreSQL 14+ (with pgvector)
- Maven 3.6+

### 配置数据库

1. 创建数据库：
   ```sql
   CREATE DATABASE mychatmind;
   ```

2. 启用 pgvector 扩展：
   ```sql
   CREATE EXTENSION vector;
   ```

3. 修改 `application.yaml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/mychatmind
       username: postgres
       password: your-password
   ```

### 运行项目

```bash
# 进入项目目录
cd MyChatMind

# 编译项目
mvn clean install

# 运行
mvn spring-boot:run
```

### API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/agents` | POST | 创建 Agent |
| `/api/agents` | GET | 获取 Agent 列表 |
| `/api/agents/{id}` | GET | 获取单个 Agent |
| `/api/agents/{id}` | PUT | 更新 Agent |
| `/api/agents/{id}` | DELETE | 删除 Agent |

## 开发进度

- [x] 项目初始化与配置
- [x] 数据库设计
- [x] 基础 CRUD 接口
- [ ] AI 模型集成
- [ ] Agent Loop 实现
- [ ] 工具调用框架
- [ ] RAG 知识库
- [ ] SSE 实时通信

## 相关项目

- 前端项目：[JChatMind UI](../JChatMind/ui)
- 参考项目：[JChatMind](../JChatMind)

## License

MIT License
