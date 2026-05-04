import React, { useState } from "react";
import { message as antdMessage } from "antd";
import { Sender } from "@ant-design/x";

interface AgentChatInputProps {
  onSend: (message: string) => void;
}

const AgentChatInput: React.FC<AgentChatInputProps> = ({ onSend }) => {
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);

  return (
    <Sender
      onSubmit={async () => {
        if (!message.trim()) {
          antdMessage.warning("请输入消息内容");
          return;
        }
        setSending(true);
        try {
          onSend(message.trim());
          setMessage("");
        } catch (error) {
          console.error("发送消息失败:", error);
          antdMessage.error("发送消息失败，请重试");
        } finally {
          setSending(false);
        }
      }}
      placeholder="输入消息..."
      value={message}
      loading={sending}
      onChange={setMessage}
    />
  );
};

export default AgentChatInput;
