package com.study.mychatmind.service.impl;

import com.study.mychatmind.agent.tools.Tool;
import com.study.mychatmind.agent.tools.ToolType;
import com.study.mychatmind.service.ToolFaceDeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ToolFacadeServiceImpl implements ToolFaceDeService {

    // Spring 自动注入所有 Tool 实现类
    private final List<Tool> tools;

    @Override
    public List<Tool> getAllTools() {
        return tools;
    }

    @Override
    public List<Tool> getFixedTools() {
        return filterByType(ToolType.FIXED);
    }

    @Override
    public List<Tool> getOptionalTools() {
        return filterByType(ToolType.OPTIONAL);
    }

    private List<Tool> filterByType(ToolType type) {
        return tools.stream()
                .filter(tool -> tool.getType() == type)
                .toList();
    }

}
