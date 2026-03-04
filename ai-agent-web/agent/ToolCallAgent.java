package com.xuenai.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuenai.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应
    private ChatResponse chatResponse;

    // 保存最近一次思考的文本
    private String lastThinkingText = "";

    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 警用 Spring AI 内置的工具调用机制，自己维护选型和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessagesList().add(userMessage);
        }
        List<Message> messagesList = getMessagesList();
        Prompt prompt = new Prompt(messagesList, chatOptions);
        try {
            ChatResponse response = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            this.chatResponse = response;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            this.lastThinkingText = result != null ? result : "";
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{}的思考: {}", getName(), result);
            log.info("{}选择了: {}个工具", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数: %s",
                            toolCall.name(),
                            toolCall.arguments()
                    ))
                    .collect(Collectors.joining("\n"));
            log.info("工具调用信息: \n{}", toolCallInfo);
            if (toolCallList.isEmpty()) {
                getMessagesList().add(assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("{} 在思考过程遇到了问题: ", e.getMessage());
            getMessagesList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage())
            );
            return false;
        }

    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!chatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessagesList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回结果
        setMessagesList(toolExecutionResult.conversationHistory());
        // 当前工具调用结果
        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String result = toolResponseMessage.getResponses().stream()
                .map(response -> String.format("工具名称: %s, 结果: %s",
                        response.name(),
                        response.responseData()
                ))
                .collect(Collectors.joining("\n"));
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(result);
        return result;
    }

    /**
     * 流式运行代理，发送结构化 JSON SSE 事件
     */
    @Override
    public SseEmitter runStream(String prompt) {
        SseEmitter emitter = new SseEmitter(180000L);
        ObjectMapper objectMapper = new ObjectMapper();
        CompletableFuture.runAsync(() -> {
            try {
                if (getState() != AgentState.IDLE) {
                    throw new RuntimeException("Cannot run agent from state: " + getState());
                }
                if (StringUtils.isBlank(prompt)) {
                    throw new RuntimeException("Cannot run agent with empty prompt");
                }
                setState(AgentState.RUNNING);
                getMessagesList().add(new UserMessage(prompt));
                try {
                    for (int i = 0; i < getMaxSteps() && getState() != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        setCurrentStep(stepNumber);
                        log.info("Executing step {} of {}", stepNumber, getMaxSteps());

                        boolean shouldAct = think();

                        if (!shouldAct) {
                            // AI 给出最终回答，无需调用工具
                            emitter.send(buildEvent(objectMapper, "result", stepNumber, lastThinkingText));
                        } else {
                            // AI 选择调用工具，发送思考事件
                            emitter.send(buildEvent(objectMapper, "thinking", stepNumber, lastThinkingText));
                            // 执行工具
                            String actResult = act();
                            emitter.send(buildEvent(objectMapper, "tool", stepNumber, actResult));
                        }
                    }
                    // 循环结束后，发送最终结果
                    if (getState() == AgentState.FINISHED) {
                        emitter.send(buildEvent(objectMapper, "result", getCurrentStep(), lastThinkingText));
                    } else if (getCurrentStep() >= getMaxSteps()) {
                        setState(AgentState.FINISHED);
                        emitter.send(buildEvent(objectMapper, "result", getCurrentStep(),
                                "已达到最大步数限制（" + getMaxSteps() + "步）"));
                    }
                    emitter.complete();
                } catch (Exception e) {
                    setState(AgentState.ERROR);
                    log.error("Error running agent", e);
                    try {
                        emitter.send(buildEvent(objectMapper, "error", getCurrentStep(),
                                "执行出错: " + e.getMessage()));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        emitter.onCompletion(() -> {
            if (getState() == AgentState.RUNNING) {
                setState(AgentState.FINISHED);
            }
            cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }

    private String buildEvent(ObjectMapper mapper, String type, int step, String content) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", type);
            event.put("step", step);
            event.put("content", content != null ? content : "");
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"content\":\"JSON序列化失败\"}";
        }
    }
}
