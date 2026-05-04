package com.study.mychatmind.agent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import com.study.mychatmind.agent.tools.Tool;

public class DirectAnswerTool implements Tool {

    @Override
    public String getName() {
        return "direct_answer";
    }

    @Override
    public String getDescription() {
        return "当可以直接回答用户问题时，使用此工具给出最终答案";
    }

    @org.springframework.ai.tool.annotation.Tool(description = "当可以直接回答用户问题时，使用此工具给出最终答案")
    public String directAnswer(String answer) {
        return answer;
    }


    @Override
    public ToolCallback toToolCallback() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks() [0];
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }



}
