# AIAgent 项目指南

本项目是一个综合性的 AI 智能体应用系统，采用了前后端分离的架构设计，同时包含由 Spring Boot 和 Vue 3 构建的核心业务模块，以及支持模型上下文协议（MCP）的扩展服务。

## 目录结构与功能模块

整个项目包含以下三大核心模块：

### 1. 核心后端 (`ai-agent`)
基于 **Spring Boot 3.4.4** 和 **Java 21** 构建的 AI Agent 服务端。
- **框架支持**：集成 Spring AI Alibaba Starter, Spring AI Ollama 等，适配 DashScope (通义千问) 等大模型生态。
- **核心组件**：
  - **Manus 智能体**：基于 `ToolCallAgent` 实现的核心 Agent，具备强大的工具调用、任务规划和自主拆解能力（支持多达 20 步的复杂链式规划），并结合了 `VectorStore` 实现 RAG (检索增强生成) 能力。
  - **丰富的可调用工具 (Tools)**：
    - `WebSearchTool`：网络搜索
    - `WebScrapingTool`：网页内容抓取
    - `FileOperationTool`：本地文件操作
    - `ResourceDownloadTool`：资源与文件下载
    - `PDFGenerationTool`：PDF 动态生成
    - `TerminalOperationTool`：终端命令执行指令下发
    - `TerminateTool`：流程中断控制
- **数据存储**：结合了 Redis (用于会话/缓存) 和 PostgreSQL (+ PGVector) 以提供持久化和向量检索支持。

### 2. 用户前端 (`ai-agent-web`)
基于 **Vue 3 + Vite** 构建的现代化交互界面。
- **技术栈**：Vue 3 (Composition API / `<script setup>`), Vue Router, Axios, Vite。
- **功能特性**：
  - 与后端打通的智能对话界面。
  - 支持 SSE（Server-Sent Events）流式响应，能够实时渲染大模型的思考过程、工具调用（Function Calling）及最终输出。
  - 支持应用内生成文件的联动下载（如 PDF 文档导出）。
  
### 3. MCP 扩展服务 (`image-search-mcp`)
基于 **Spring AI MCP Server** 规范实现的一款独立的模型上下文协议（Model Context Protocol）微服务。
- 专门用于提供“图像搜索”能力的扩展支持，使主体的 AI Agent 能够通过 MCP 协议动态获取外部图片检索结果。

## 快速开始

### 环境依赖
1. **JDK 21**及以上版本。
2. **Node.js** (推荐 18+)。
3. **Redis** & **PostgreSQL 配合 pgvector 插件** (用于向量数据库)。

### 后端运行 (ai-agent)
在项目根目录（或 `ai-agent` 目录）下，配置好 `application.yml` 中的数据库和大模型 API 密钥信息后，通过 Maven 启动：
```bash
./mvnw spring-boot:run
```

### 前端运行 (ai-agent-web)
进入前端目录，安装依赖并启动开发服务器：
```bash
cd ai-agent-web
npm install
npm run dev
```

### MCP 运行 (image-search-mcp)
进入独立服务目录启动 MCP Server：
```bash
cd image-search-mcp
./mvnw spring-boot:run
```

## 设计亮点与技术演进

- **流式思考呈现**：前端可动态解析带有思考步骤及系统工具调用的数据流，实现直观的“AI 处理日志”展示。
- **动态工具链**：高度解耦的 Agent 工具注册机制，后续可轻易通过 MCP 或原生 Java 代码接入新的 Tool 能力。
- **多模型兼容**：借助 Spring AI 抽象和 DashScope SDK 配置，兼顾不同模型服务商及本地 Ollama 大语言模型的调用需求。

## 许可证
本项目为开源项目，具体使用许可详见代码库中的声明。
