package com.study.mychatmind.service;

import com.study.mychatmind.model.request.CreateChatSessionRequest;
import com.study.mychatmind.model.request.UpdateChatSessionRequest;
import com.study.mychatmind.model.response.CreateChatSessionResponse;
import com.study.mychatmind.model.response.GetChatSessionResponse;
import com.study.mychatmind.model.response.GetChatSessionsResponse;

public interface ChatSessionFacadeService {
    GetChatSessionsResponse getChatSessions();

    GetChatSessionResponse getChatSession(String chatSessionId);

    GetChatSessionsResponse getChatSessionsByAgentId(String agentId);

    CreateChatSessionResponse createChatSession(CreateChatSessionRequest request);

    void deleteChatSession(String chatSessionId);

    void updateChatSession(String chatSessionId, UpdateChatSessionRequest request);
}
