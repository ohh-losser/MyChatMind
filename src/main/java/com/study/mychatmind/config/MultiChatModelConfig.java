package com.study.mychatmind.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多模型配置类
 *
 * 为每个 AI 模型创建 ChatModel Bean，Bean 名称用于注册表查找
 */
@Configuration
public class MultiChatModelConfig {

    /**
     * DeepSeek 模型
     */
    @Bean("deepseek-chat")
    public ChatModel deepSeekChatModel(DeepSeekChatModel model) {
        return model;
    }

    /**
     * 智谱 AI 模型
     */
    @Bean("glm-4.6")
    public ChatModel zhiPuAiChatModel(ZhiPuAiChatModel model) {
        return model;
    }

    /**
     * 无问芯穹模型（通过 OpenAI 兼容接口）
     */
    @Bean("infini-ai")
    public ChatModel infiniAiChatModel(OpenAiChatModel model) {
        return model;
    }
}
