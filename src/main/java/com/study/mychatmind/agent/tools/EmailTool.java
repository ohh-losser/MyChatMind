package com.study.mychatmind.agent.tools;

import com.study.mychatmind.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

/**
 * 邮件发送工具 - 可选工具（按 Agent 配置绑定）
 */
@Component
@Slf4j
public class EmailTool implements Tool {

    private final EmailService emailService;

    public EmailTool(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public String getName() {
        return "emailTool";
    }

    @Override
    public String getDescription() {
        return "一个用于发送邮件的工具，可以通过邮箱发送邮件给指定的收件人";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    @Override
    public ToolCallback toToolCallback() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks()[0];
    }

    /**
     * 发送邮件
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "sendEmail",
            description = "发送邮件到指定的收件人。参数包括：to（收件人邮箱地址，必填）、subject（邮件主题，必填）、content（邮件正文内容，必填）"
    )
    public String sendEmail(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty()) {
            return "错误：收件人邮箱地址不能为空";
        }
        if (subject == null || subject.trim().isEmpty()) {
            return "错误：邮件主题不能为空";
        }
        if (content == null || content.trim().isEmpty()) {
            return "错误：邮件内容不能为空";
        }
        if (!to.contains("@")) {
            return "错误：收件人邮箱地址格式不正确";
        }

        emailService.sendEmailAsync(to.trim(), subject.trim(), content.trim());
        log.info("邮件已提交发送, 收件人: {}, 主题: {}", to, subject);
        return String.format("邮件已提交发送！\n收件人: %s\n主题: %s", to, subject);
    }
}