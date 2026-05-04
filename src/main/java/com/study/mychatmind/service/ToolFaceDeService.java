package com.study.mychatmind.service;

import com.study.mychatmind.agent.tools.Tool;

import java.util.List;

public interface ToolFaceDeService {

    List<Tool> getAllTools();

    List<Tool> getFixedTools();

    List<Tool> getOptionalTools();
}


