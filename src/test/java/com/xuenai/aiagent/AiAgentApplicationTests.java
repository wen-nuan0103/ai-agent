package com.xuenai.aiagent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiAgentApplicationTests {

    @Resource
    private ChatModel dashscopeChatModel;
    
    @Test
    void contextLoads() {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("你是谁"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }

}
