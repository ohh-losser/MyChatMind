package com.study.mychatmind.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.mychatmind.agent.tools.Tool;
import com.study.mychatmind.config.ChatModelRegistry;
import com.study.mychatmind.converter.AgentConverter;
import com.study.mychatmind.converter.ChatMessageConverter;
import com.study.mychatmind.converter.KnowledgeBaseConverter;
import com.study.mychatmind.mapper.AgentMapper;
import com.study.mychatmind.mapper.KnowledgeBaseMapper;
import com.study.mychatmind.model.dto.AgentDTO;
import com.study.mychatmind.model.dto.KnowledgeBaseDTO;
import com.study.mychatmind.model.entity.Agent;
import com.study.mychatmind.model.entity.KnowledgeBase;
import com.study.mychatmind.service.ChatMessageFacadeService;
import com.study.mychatmind.service.SseService;
import com.study.mychatmind.service.ToolFaceDeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ChatAgentAFactory {

    private final AgentMapper agentMapper;
    private final AgentConverter agentConverter;
    private final ChatModelRegistry chatModelRegistry;
    private final ChatMessageFacadeService messageService;
    private final ToolFaceDeService toolFacadeService;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseConverter knowledgeBaseConverter;
    private final SseService sseService;
    private final ChatMessageConverter chatMessageConverter;

    public MyChatMindAgent createAgent(String agentId, String sessionId) {
        // 1. 查询 Agent 配置
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("Agent 不存在: " + agentId);
        }

        // 2. 转换为 DTO
        AgentDTO agentConfig;
        try {
            agentConfig = agentConverter.toDTO(agent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析 Agent 配置失败", e);
        }

        // 3. 解析运行时工具（固定工具 + 可选工具）
        List<Tool> runtimeTools = resolveRuntimeTools(agentConfig);
        List<ToolCallback> toolCallbacks = buildToolCallbacks(runtimeTools);

        // 4. 解析 Agent 可访问的知识库
        List<KnowledgeBaseDTO> knowledgeBases = resolveKnowledgeBases(agentConfig);

        log.info("创建 Agent: {}, 模型: {}, 工具: {}, 知识库: {}",
                agentConfig.getName(), agentConfig.getModel(),
                runtimeTools.stream().map(Tool::getName).toList(),
                knowledgeBases.stream().map(KnowledgeBaseDTO::getName).toList());

        // 5. 创建 Agent 实例
        return new MyChatMindAgent(agentConfig, sessionId, chatModelRegistry, messageService, toolCallbacks, knowledgeBases, sseService, chatMessageConverter);
    }

    /**
     * 解析运行时工具：固定工具 + 可选工具
     */
    private List<Tool> resolveRuntimeTools(AgentDTO agentConfig) {
        // 固定工具（所有 Agent 都有）
        List<Tool> runtimeTools = new ArrayList<>(toolFacadeService.getFixedTools());

        // 可选工具（按 Agent 配置绑定）
        List<String> allowedToolNames = agentConfig.getAllowedTools();
        if (allowedToolNames == null || allowedToolNames.isEmpty()) {
            return runtimeTools;
        }

        Map<String, Tool> optionalToolMap = toolFacadeService.getOptionalTools()
                .stream()
                .collect(Collectors.toMap(Tool::getName, Function.identity()));

        for (String toolName : allowedToolNames) {
            Tool tool = optionalToolMap.get(toolName);
            if (tool != null) {
                runtimeTools.add(tool);
            }
        }

        return runtimeTools;
    }

    /**
     * 解析 Agent 可访问的知识库
     */
    private List<KnowledgeBaseDTO> resolveKnowledgeBases(AgentDTO agentConfig) {
        List<String> allowedKbIds = agentConfig.getAllowedKbs();
        if (allowedKbIds == null || allowedKbIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectByIdBatch(allowedKbIds);
        if (knowledgeBases.isEmpty()) {
            return Collections.emptyList();
        }

        List<KnowledgeBaseDTO> kbDTOs = new ArrayList<>();
        try {
            for (KnowledgeBase knowledgeBase : knowledgeBases) {
                KnowledgeBaseDTO kbDTO = knowledgeBaseConverter.toDTO(knowledgeBase);
                kbDTOs.add(kbDTO);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析知识库失败", e);
        }
        return kbDTOs;
    }

    /**
     * 将 Tool 列表转为 ToolCallback 列表
     */
    private List<ToolCallback> buildToolCallbacks(List<Tool> runtimeTools) {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (Tool tool : runtimeTools) {
            Object target = resolveToolTarget(tool);
            ToolCallback[] toolCallbackArray = MethodToolCallbackProvider.builder()
                    .toolObjects(target)
                    .build()
                    .getToolCallbacks();
            callbacks.addAll(Arrays.asList(toolCallbackArray));
        }
        return callbacks;
    }

    /**
     * 解析工具目标对象（处理 AOP 代理）
     */
    private Object resolveToolTarget(Tool tool) {
        try {
            return AopUtils.isAopProxy(tool)
                    ? AopUtils.getTargetClass(tool)
                    : tool;
        } catch (Exception e) {
            throw new IllegalStateException("解析工具目标对象失败: " + tool.getName(), e);
        }
    }
}
