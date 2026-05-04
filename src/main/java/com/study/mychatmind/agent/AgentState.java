package com.study.mychatmind.agent;

import lombok.Getter;

@Getter
public enum AgentState {

    IDLE("空闲，等待用户输入"),
    THINKING("思考中，等待 AI 返回决策"),
    EXECUTING("执行中， 运行工具"),
    FINISHED("已完成，可以返回结果"),
    ERROR("错误， 执行异常");

    private final String description;

    AgentState(String description) {
        this.description = description;
    }
}
