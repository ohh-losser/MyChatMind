package com.study.mychatmind.agent;

import com.study.mychatmind.config.ChatModelRegistry;
import com.study.mychatmind.converter.ChatMessageConverter;
import com.study.mychatmind.message.SseMessage;
import com.study.mychatmind.model.dto.AgentDTO;
import com.study.mychatmind.model.dto.ChatMessageDTO;
import com.study.mychatmind.model.dto.KnowledgeBaseDTO;
import com.study.mychatmind.model.vo.ChatMessageVO;
import com.study.mychatmind.service.ChatMessageFacadeService;
import com.study.mychatmind.service.SseService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class MyChatMindAgent {

    private final AgentDTO agentConfig;
    private final ChatModel chatModel;
    private final String sessionId;
    private final ChatMessageFacadeService messageService;
    private final List<ToolCallback> toolCallbacks;
    private final List<KnowledgeBaseDTO> availableKbs;
    private final SseService sseService;
    private final ChatMessageConverter chatMessageConverter;

    private final List<Message> conversationHistory = new ArrayList<>();
    private final List<ChatMessageDTO> pendingMessages = new ArrayList<>();

    private AgentState state = AgentState.IDLE;
    private static final int MAX_STEPS = 20;
    private int currentStep = 0;

    public MyChatMindAgent(
            AgentDTO agentConfig,
            String sessionId,
            ChatModelRegistry modelRegistry,
            ChatMessageFacadeService messageService,
            List<ToolCallback> toolCallbacks,
            List<KnowledgeBaseDTO> availableKbs,
            SseService sseService,
            ChatMessageConverter chatMessageConverter) {

        this.agentConfig = agentConfig;
        this.chatModel = modelRegistry.get(agentConfig.getModel().getModelName());
        this.sessionId = sessionId;
        this.messageService = messageService;
        this.toolCallbacks = toolCallbacks;
        this.availableKbs = availableKbs;
        this.sseService = sseService;
        this.chatMessageConverter = chatMessageConverter;

        if (agentConfig.getSystemPrompt() != null) {
            conversationHistory.add(new SystemMessage(agentConfig.getSystemPrompt()));
        }

        log.info("Agent 初始化完成, 模型: {}, 工具数量: {}, 知识库数量: {}, sessionId: {}",
                agentConfig.getModel(), toolCallbacks.size(), availableKbs.size(), sessionId);
    }

    /**
     * 执行 Agent - Think-Execute 循环
     */
    public String run(String userInput) {
        log.info("Agent 开始运行, 用户输入: {}", userInput);

        try {
            state = AgentState.THINKING;
            currentStep = 0;

            // 保存用户消息
            saveUserMessage(userInput);
            conversationHistory.add(new UserMessage(userInput));

            // Think-Execute 循环
            for (int i = 0; i < MAX_STEPS; i++) {
                currentStep = i;

                // Think: AI 思考决策
                boolean needExecute = think();

                if (needExecute) {
                    // Execute: 执行工具调用
                    state = AgentState.EXECUTING;
                    execute();
                    state = AgentState.THINKING;
                } else {
                    // AI 不再调用工具 → 任务完成
                    state = AgentState.FINISHED;
                    break;
                }
            }

            // 推送完成状态
            pushDoneStatus();

            // 返回最后的 AI 回复
            return getLastAssistantContent();

        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Agent 执行出错: {}", e.getMessage(), e);
            // 推送完成状态（即使出错也要关闭前端的加载状态）
            pushDoneStatus();
            throw new RuntimeException("Agent 执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * Think: AI 思考决策
     * 返回 true 表示需要执行工具，false 表示任务完成
     */
    private boolean think() {
        log.info("Think - 第 {} 步", currentStep + 1);

        // 推送思考状态
        pushStatusMessage(SseMessage.Type.AI_THINKING, "正在思考...");

        // 构建知识库提示信息，作为额外的系统提示
        String kbPrompt = buildKnowledgeBasePrompt();

        ChatOptions chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .toolCallbacks(toolCallbacks)
                .build();

        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 添加知识库提示（无论是否有知识库都要添加）
        messages.add(new SystemMessage(kbPrompt));

        // 添加对话历史
        messages.addAll(conversationHistory);

        Prompt prompt = new Prompt(messages, chatOptions);

        // 调用模型
        ChatResponse response = chatModel.call(prompt);

        AssistantMessage output = response.getResult().getOutput();

        // 添加到对话历史
        conversationHistory.add(output);

        // 保存 AI 消息
        saveAssistantMessage(output);

        // 判断是否需要调用工具
        boolean hasToolCalls = output.getToolCalls() != null && !output.getToolCalls().isEmpty();
        log.info("Think 结果: {}", hasToolCalls ? "需要调用工具" : "任务完成");
        return hasToolCalls;
    }

    /**
     * 构建知识库提示信息
     */
    private String buildKnowledgeBasePrompt() {
        if (availableKbs == null || availableKbs.isEmpty()) {
            // 没有绑定知识库时，明确告诉 AI 不要使用 KnowledgeTool
            return """

                    【知识库信息】
                    当前 Agent 没有绑定任何知识库。
                    请不要调用 KnowledgeTool 工具，因为你没有可访问的知识库。
                    直接根据你自己的知识回答用户问题即可。
                    """;
        }

        String kbInfo = availableKbs.stream()
                .map(kb -> String.format("- 知识库ID: %s, 名称: %s, 描述: %s",
                        kb.getId(), kb.getName(), kb.getDescription() != null ? kb.getDescription() : "无"))
                .collect(Collectors.joining("\n"));

        return String.format("""

                【知识库信息】
                你目前可以访问以下知识库：
                %s

                如果用户的问题涉及到知识库中的内容，请使用 KnowledgeTool 工具进行检索。
                调用 KnowledgeTool 时需要传入正确的知识库ID（kbId）和查询文本（query）。
                知识库ID必须是上面列出的有效ID，不要使用无效的ID如"0"。
                """, kbInfo);
    }

    /**
     * Execute: 执行工具调用
     */
    private void execute() {
        AssistantMessage lastMessage = (AssistantMessage) conversationHistory.get(conversationHistory.size() - 1);
        List<AssistantMessage.ToolCall> toolCalls = lastMessage.getToolCalls();

        log.info("Execute - 需要执行 {} 个工具调用", toolCalls.size());

        // 推送执行状态
        String toolNames = toolCalls.stream()
                .map(AssistantMessage.ToolCall::name)
                .collect(Collectors.joining(", "));
        pushStatusMessage(SseMessage.Type.AI_EXECUTING, "正在执行工具: " + toolNames);

        List<org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse> responses = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            log.info("执行工具: {}, 参数: {}", toolCall.name(), toolCall.arguments());

            String result = executeToolCall(toolCall);
            log.info("工具执行结果: {}", result);

            saveToolMessage(result);

            responses.add(new org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse(
                    toolCall.id(), toolCall.name(), result
            ));
        }

        conversationHistory.add(org.springframework.ai.chat.messages.ToolResponseMessage.builder()
                .responses(responses)
                .build());
    }

    /**
     * 执行单个工具调用
     */
    private String executeToolCall(AssistantMessage.ToolCall toolCall) {
        for (ToolCallback callback : toolCallbacks) {
            if (callback.getToolDefinition().name().equals(toolCall.name())) {
                try {
                    return callback.call(toolCall.arguments());
                } catch (Exception e) {
                    log.error("工具执行失败: {}, 错误: {}", toolCall.name(), e.getMessage());
                    return "工具执行失败: " + e.getMessage();
                }
            }
        }
        return "未找到工具: " + toolCall.name();
    }

    /**
     * 获取最后一条 AssistantMessage 的文本内容
     */
    private String getLastAssistantContent() {
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            Message msg = conversationHistory.get(i);
            if (msg instanceof AssistantMessage assistantMsg) {
                return assistantMsg.getText();
            }
        }
        return "";
    }

    /**
     * 保存用户消息
     */
    private void saveUserMessage(String content) {
        ChatMessageDTO message = ChatMessageDTO.builder()
                .sessionId(sessionId)
                .role(ChatMessageDTO.RoleType.USER)
                .content(content)
                .build();
        messageService.createChatMessage(message);
    }

    /**
     * 保存 AI 消息
     */
    private void saveAssistantMessage(AssistantMessage output) {
        ChatMessageDTO message = ChatMessageDTO.builder()
                .sessionId(sessionId)
                .role(ChatMessageDTO.RoleType.ASSISTANT)
                .content(output.getText())
                .metadata(ChatMessageDTO.MetaData.builder()
                        .toolCalls(output.getToolCalls())
                        .build())
                .build();

        // 持久化并获取 ID
        var response = messageService.createChatMessage(message);
        message.setId(response.getChatMessageId());

        // 推送给前端
        pushToSse(message, SseMessage.Type.AI_GENERATED_CONTENT);
    }

    /**
     * 保存工具执行结果消息
     */
    private void saveToolMessage(String content) {
        ChatMessageDTO message = ChatMessageDTO.builder()
                .sessionId(sessionId)
                .role(ChatMessageDTO.RoleType.TOOL)
                .content(content)
                .build();

        // 持久化并获取 ID
        var response = messageService.createChatMessage(message);
        message.setId(response.getChatMessageId());

        // 推送给前端
        pushToSse(message, SseMessage.Type.AI_EXECUTING);
    }

    /**
     * 通过 SSE 推送消息给前端
     */
    private void pushToSse(ChatMessageDTO message, SseMessage.Type type) {
        ChatMessageVO vo = chatMessageConverter.toVO(message);
        SseMessage sseMessage = SseMessage.builder()
                .type(type)
                .payload(SseMessage.Payload.builder()
                        .message(vo)
                        .build())
                .metadata(SseMessage.Metadata.builder()
                        .chatMessageId(message.getId())
                        .build())
                .build();
        sseService.send(sessionId, sseMessage);
    }

    /**
     * 推送完成状态给前端
     */
    private void pushDoneStatus() {
        SseMessage sseMessage = SseMessage.builder()
                .type(SseMessage.Type.AI_DONE)
                .payload(SseMessage.Payload.builder()
                        .done(true)
                        .build())
                .build();
        sseService.send(sessionId, sseMessage);
    }

    /**
     * 推送状态消息给前端（思考/执行）
     */
    private void pushStatusMessage(SseMessage.Type type, String statusText) {
        SseMessage sseMessage = SseMessage.builder()
                .type(type)
                .payload(SseMessage.Payload.builder()
                        .statusText(statusText)
                        .build())
                .build();
        sseService.send(sessionId, sseMessage);
    }
}
