package com.study.mychatmind.agent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class TerminateTool implements Tool {
    @Override
    public String getName() {
        return "terminate";
    }

    @Override
    public String getDescription() {
        return "跳出 Agent Loop 的工具";
    }

    @org.springframework.ai.tool.annotation.Tool(name = "terminate", description = "如果你觉得当前所有的任务已经执行完毕了，就执行这个工具调用")
    public void terminate(){}

    @Override
    public ToolCallback toToolCallback() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks()[0];

    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }
}
