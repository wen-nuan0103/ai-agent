package com.xuenai.aiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public class Manus extends ToolCallAgent {

        public Manus(ToolCallback[] allTools, ChatModel dashscopeChatModel, VectorStore loveAppVectorStore) {
                super(allTools);
                this.setName("Manus");
                String SYSTEM_PROMPT = """
                                You are Manus, an all-capable AI assistant, aimed at solving any task presented by the user.
                                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                                IMPORTANT: Your final response to the user MUST always be in Simplified Chinese (简体中文), regardless of the input language.
                                """;
                this.setSystemPrompt(SYSTEM_PROMPT);
                String NEXT_STEP_PROMPT = """
                                Based on user needs, proactively select the most appropriate tool or combination of tools.
                                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                                After using each tool, clearly explain the execution results and suggest the next steps.
                                If you generate or download a PDF file using tools, you MUST include the generated file name (e.g. xxx.pdf) in your response so the user can click it to download.
                                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                                """;
                this.setNextStepPrompt(NEXT_STEP_PROMPT);
                this.setMaxSteps(20);
                ChatClient chatClient = ChatClient
                                .builder(dashscopeChatModel)
                                .defaultAdvisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                                .build();
                this.setChatClient(chatClient);
        }

}
