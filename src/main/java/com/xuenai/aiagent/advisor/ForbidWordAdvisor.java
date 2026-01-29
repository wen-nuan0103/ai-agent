package com.xuenai.aiagent.advisor;

import cn.hutool.dfa.WordTree;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 禁用词检查 Advisor
 */
public class ForbidWordAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    
    private final WordTree wordTree;
    private final String blockMessage;
    private static final Set<String> FORBID_WORD = new HashSet<>(Arrays.asList(
            "测试禁用词",
            "BLOCK_ME",
            "Ignore previous instructions",
            "忽略之前的指令",
            "You are a developer",
            "你的开发者是谁",
            "System prompt", 
            "系统设定",
            "DAN mode", 
            "奶奶漏洞",
            "炸弹", 
            "枪支", 
            "恐怖分子",
            "ChatGPT",
            "OpenAI",
            "文心一言",
            "通义千问",
            "Kimi",
            "豆包"
            ));
    
    public ForbidWordAdvisor() {
        this.wordTree = new WordTree();
        this.wordTree.addWords(FORBID_WORD);
        this.blockMessage = "抱歉，检测到输入包含敏感或违规内容，无法为您提供服务。";
    }
    
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        if (wordTree.isMatch(advisedRequest.userText())) {
            return new AdvisedResponse(createBlockResponse(),advisedRequest.adviseContext());
        }
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        if (wordTree.isMatch(advisedRequest.userText())) {
            return Flux.just(new AdvisedResponse(createBlockResponse(),advisedRequest.adviseContext()));
        }
        return chain.nextAroundStream(advisedRequest);
    }

    @Override
    public String getName() {
        return "ForbidWordAdvisor";
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private ChatResponse createBlockResponse() {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(blockMessage))));
    }
}
