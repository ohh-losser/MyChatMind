package com.study.mychatmind.controller;

import com.study.mychatmind.config.ChatModelRegistry;
import com.study.mychatmind.model.common.ApiResponse;
import com.study.mychatmind.model.request.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 聊天接口 - 使用 ChatModel + 注册表模式
 *
 * 支持动态选择模型：/api/chat/{model}
 * 可用模型：deepseek-chat, glm-4.6, infini-ai
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatModelRegistry registry;

    public ChatController(ChatModelRegistry registry) {
        this.registry = registry;
    }

    /**
     * 获取所有可用的模型
     */
    @GetMapping("/models")
    public ApiResponse<Set<String>> getAvailableModels() {
        return ApiResponse.success(registry.getAvailableModels());
    }

    /**
     * 简单聊天接口
     *
     * @param model 模型名称（deepseek-chat, glm-4.6, infini-ai）
     */
    @PostMapping("/{model}")
    public ApiResponse<String> chat(@PathVariable String model, @RequestBody ChatRequest request) {
        log.info("收到聊天请求, 模型: {}, 消息: {}", model, request.getMessage());

        ChatModel chatModel = registry.get(model);
        Prompt prompt = new Prompt(request.getMessage());
        ChatResponse response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getText();

        log.info("AI 回复: {}", content);
        return ApiResponse.success(content);
    }

    /**
     * 带系统提示词的聊天接口
     */
    @PostMapping("/{model}/with-system")
    public ApiResponse<String> chatWithSystem(@PathVariable String model, @RequestBody ChatRequest request) {
        log.info("收到聊天请求, 模型: {}, 消息: {}, 系统提示词: {}", model, request.getMessage(), request.getSystemPrompt());

        ChatModel chatModel = registry.get(model);

        List<Message> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }
        messages.add(new UserMessage(request.getMessage()));

        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getText();

        log.info("AI 回复: {}", content);
        return ApiResponse.success(content);
    }
}
