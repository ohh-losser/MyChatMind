package com.study.mychatmind.controller;

import com.study.mychatmind.agent.ChatAgentAFactory;
import com.study.mychatmind.agent.ChatAgentAFactory;
import com.study.mychatmind.agent.MyChatMindAgent;
import com.study.mychatmind.model.common.ApiResponse;
import com.study.mychatmind.model.request.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Agent 运行接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@AllArgsConstructor
public class AgentRunController {

    private final ChatAgentAFactory agentFactory;

    /**
     * 运行 Agent
     *
     * @param agentId   Agent ID
     * @param sessionId 会话 ID
     * @param request   用户消息
     */
    @PostMapping("/{agentId}/session/{sessionId}/run")
    public ApiResponse<String> runAgent(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody ChatRequest request) {

        log.info("运行 Agent: {}, sessionId: {}", agentId, sessionId);

        // 1. 创建 Agent 实例
        MyChatMindAgent agent = agentFactory.createAgent(agentId, sessionId);

        // 2. 执行 Agent
        String response = agent.run(request.getMessage());

        return ApiResponse.success(response);
    }
}
