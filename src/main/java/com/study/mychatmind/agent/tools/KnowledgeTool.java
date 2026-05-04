package com.study.mychatmind.agent.tools;

import com.study.mychatmind.service.RagService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库检索工具
 * Agent 可以调用此工具从知识库中检索相关信息
 */
@Component
public class KnowledgeTool implements Tool {

    private final RagService ragService;

    public KnowledgeTool(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public String getName() {
        return "KnowledgeTool";
    }

    @Override
    public String getDescription() {
        return "用于从知识库执行语义检索（RAG）。输入知识库 ID 和查询文本，返回与查询最相关的内容片段。";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;  // 固定工具，所有 Agent 都有
    }

    @Override
    public ToolCallback toToolCallback() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks()[0];
    }

    /**
     * 从知识库中检索相关内容
     *
     * @param kbId  知识库 ID
     * @param query 查询文本
     * @return 与查询最相关的知识片段（多个片段用换行分隔）
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "KnowledgeTool",
            description = "从指定知识库中执行相似性检索（RAG）。参数为知识库 ID（kbId）和查询文本（query），返回与查询最相关的知识片段。"
    )
    public String knowledgeQuery(String kbId, String query) {
        List<String> results = ragService.similaritySearch(kbId, query);
        if (results == null || results.isEmpty()) {
            return "未找到相关知识片段";
        }
        return String.join("\n\n", results);
    }
}
