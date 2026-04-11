package com.study.mychatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.study.mychatmind.converter.AgentConverter;
import com.study.mychatmind.exception.BizException;
import com.study.mychatmind.mapper.AgentMapper;
import com.study.mychatmind.model.dto.AgentDTO;
import com.study.mychatmind.model.entity.Agent;
import com.study.mychatmind.model.request.CreateAgentRequest;
import com.study.mychatmind.model.request.UpdateAgentRequest;
import com.study.mychatmind.model.response.CreateAgentResponse;
import com.study.mychatmind.model.response.GetAgentsResponse;
import com.study.mychatmind.model.vo.AgentVO;
import com.study.mychatmind.service.AgentFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AgentFacadeServiceImpl implements AgentFacadeService {

    private final AgentMapper agentMapper;
    private final AgentConverter agentConverter;

    @Override
    public GetAgentsResponse getAgents() {
        List<Agent> agents = agentMapper.selectAll();
        List<AgentVO> result = new ArrayList<>();
        for (Agent agent : agents) {
            try {
                AgentVO vo = agentConverter.toVO(agent);
                result.add(vo);
            } catch (JsonProcessingException e) {
                     throw new RuntimeException(e);
            }
        }
        return GetAgentsResponse.builder()
               .agents(result.toArray(new AgentVO[0]))
               .build();
    }

    @Override
    public CreateAgentResponse createAgent(CreateAgentRequest request) {
        try {
            // 将 CreateAgentRequest 转换为 AgentDTO
            AgentDTO agentDTO = agentConverter.toDTO(request);

            Agent agent = agentConverter.toEntity(agentDTO);

            LocalDateTime now = LocalDateTime.now();
            agent.setCreatedAt(now);
            agent.setUpdatedAt(now);

            int result = agentMapper.insert(agent);
            if (result == 0) {
                throw new RuntimeException("创建失败");
            }

            return CreateAgentResponse.builder()
                    .agentId(agent.getId())
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAgent(String agentId) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new RuntimeException("Agent 不存在: " + agentId);
        }

        int result = agentMapper.deleteById(agentId);
        if(result <= 0) {
            throw new BizException("删除 agent 失败");
        }
    }

    @Override
    public void updateAgent(String agentId, UpdateAgentRequest request) {

        try{
            Agent existingAgent = agentMapper.selectById(agentId);
            if (existingAgent == null) {
                throw new RuntimeException("Agent 不存在: " + agentId);
            }

            AgentDTO agentDTO = agentConverter.toDTO(existingAgent);

            agentConverter.updateDTOFromRequest(agentDTO, request);

            Agent updatedAgent = agentConverter.toEntity(agentDTO);

            updatedAgent.setId(existingAgent.getId());
            updatedAgent.setCreatedAt(existingAgent.getCreatedAt());
            updatedAgent.setUpdatedAt(LocalDateTime.now());

            int result = agentMapper.updateById(updatedAgent);
            if (result <= 0) {
                throw new RuntimeException("更新失败");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("更新 agent 时发生序列化异常" +  e.getMessage());
        }
    }
}
