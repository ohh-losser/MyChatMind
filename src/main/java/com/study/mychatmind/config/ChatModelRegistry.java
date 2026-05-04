package com.study.mychatmind.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ChatModel 注册表
 *
 * 统一管理多个 ChatModel 实例，支持运行时动态选择模型
 * Spring 会自动注入所有 ChatModel Bean 到 Map 中
 */
@Component
public class ChatModelRegistry {

    private final Map<String, ChatModel> chatModels;

    public ChatModelRegistry(Map<String, ChatModel> chatModels) {
        this.chatModels = chatModels;
    }

    /**
     * 根据名称获取 ChatModel
     * @param key 模型名称（Bean 名称）
     * @return ChatModel 实例
     */
    public ChatModel get(String key) {
        ChatModel model = chatModels.get(key);
        if (model == null) {
            throw new IllegalArgumentException("未找到模型: " + key + ", 可用模型: " + chatModels.keySet());
        }
        return model;
    }

    /**
     * 获取所有可用的模型名称
     */
    public java.util.Set<String> getAvailableModels() {
        return chatModels.keySet();
    }
}
