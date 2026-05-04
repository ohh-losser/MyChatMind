package com.study.mychatmind.model.request;

import lombok.Data;

/**
 * 聊天请求对象
 */
@Data
public class ChatRequest {
    /**
     * 用户消息
     */
    private String message;

    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;
}
