package com.study.mychatmind.service;

import com.study.mychatmind.message.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    // 用 chatSessionId 作为连接标识
    SseEmitter connect(String chatSessionId);

    void send(String chatSessionId, SseMessage message);
}