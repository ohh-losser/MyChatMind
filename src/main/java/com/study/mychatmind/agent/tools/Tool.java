package com.study.mychatmind.agent.tools;

import org.springframework.ai.tool.ToolCallback;

public interface Tool {

    String getName();

    String getDescription();

    ToolCallback toToolCallback();

    ToolType getType();
}
