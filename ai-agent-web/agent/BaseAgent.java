package com.xuenai.aiagent.agent;

import com.xuenai.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 */

@Slf4j
@Data
public abstract class BaseAgent {

    private String name;
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // 会话上下文
    private List<Message> messagesList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param prompt 用户提示词
     * @return 执行结果
     */
    public String run(String prompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtils.isBlank(prompt)) {
            throw new RuntimeException("Cannot run agent with empty prompt");
        }
        state = AgentState.RUNNING;
        messagesList.add(new UserMessage(prompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {} of {}", stepNumber, maxSteps);

                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps ( " + maxSteps + " )");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error running agent", e);
            return "Error running agent: " + e.getMessage();
        } finally {
            cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param prompt 用户提示词
     * @return SseEmitter 实例
     */
    public SseEmitter runStream(String prompt) {
        SseEmitter emitter = new SseEmitter(180000L);
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    throw new RuntimeException("Cannot run agent from state: " + this.state);
                }
                if (StringUtils.isBlank(prompt)) {
                    throw new RuntimeException("Cannot run agent with empty prompt");
                }
                state = AgentState.RUNNING;
                messagesList.add(new UserMessage(prompt));
                try {
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step {} of {}", stepNumber, maxSteps);

                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;
                        emitter.send(result);
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        emitter.send("Terminated: Reached max steps ( " + maxSteps + " )");
                    }
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("Error running agent", e);
                    try {
                        emitter.send("Error running agent: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        
        return emitter;
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
    }

    ;

}
