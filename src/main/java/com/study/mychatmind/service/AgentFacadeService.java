package com.study.mychatmind.service;

import com.study.mychatmind.model.request.CreateAgentRequest;
import com.study.mychatmind.model.request.UpdateAgentRequest;
import com.study.mychatmind.model.response.CreateAgentResponse;
import com.study.mychatmind.model.response.GetAgentsResponse;

public interface AgentFacadeService {
    GetAgentsResponse getAgents();

    CreateAgentResponse createAgent(CreateAgentRequest request);

    void deleteAgent(String agentId);

    void updateAgent(String agentId, UpdateAgentRequest request);
}
