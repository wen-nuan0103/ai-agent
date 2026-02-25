package com.xuenai.aiagent.app;

import com.xuenai.aiagent.advisor.ForbidWordAdvisor;
import com.xuenai.aiagent.chatmemory.RedisChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class LoveApp {

    private final ChatClient chatClient;
    private VectorStore vectorStore;
    private Advisor cloudAdvisor;

    @Value("classpath:prompts/love_gentle.st")
    private Resource gentleResource;
    @Value("classpath:prompts/love_strategy.st")
    private Resource strategyResource;
    @Value("classpath:prompts/love_sober.st")
    private Resource soberResource;

    private static final String SYSTEM_PROMPT = """
            扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。
            围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；
            恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。
            引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。
            """;

    public LoveApp(ChatModel dashscopeChatModel, RedisChatMemory redisChatMemory,
                   VectorStore loveAppVectorStore, Advisor loveAppCloudAdvisor) {
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new ForbidWordAdvisor()
//                        new MessageChatMemoryAdvisor(redisChatMemory)
                )
                .build();
        this.vectorStore = loveAppVectorStore;
        this.cloudAdvisor = loveAppCloudAdvisor;
    }

    public String chat(String userMessage) {
        ChatResponse response = chatClient.prompt()
                .user(userMessage)
                .advisors(spec ->
                        spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "love")
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                )
                .call().chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    public String chat(String userMessage, String userName, String userStatus) {
        PromptTemplate promptTemplate = new PromptTemplate(gentleResource);
        Message systemMessage = promptTemplate.createMessage(Map.of(
                "user_name", userName,
                "user_status", userStatus
        ));
        return chatClient.prompt()
                .system(systemMessage.getText())
                .user(userMessage)
                .advisors(spec ->
                        spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "love:" + userName)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .call()
                .content();

    }

    public String chatWithRag(String userMessage) {
        ChatResponse response = chatClient
                .prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "love")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    public String chatWithCloudRag(String userMessage) {
        ChatResponse response = chatClient
                .prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "love")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(cloudAdvisor)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

}
