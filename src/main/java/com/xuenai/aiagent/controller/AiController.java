package com.xuenai.aiagent.controller;

import com.xuenai.aiagent.agent.Manus;
import com.xuenai.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RequestMapping("/ai")
@RestController
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private Manus manus;

    @GetMapping("/love/chat/sync")
    public String doChatWithLoveApp(String userMessage, String chatId) {
        return loveApp.chat(userMessage, chatId);
    }

    @GetMapping(value = "/love/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String userMessage, String chatId) {
        return loveApp.chatWithStream(userMessage, chatId);
    }

    @GetMapping(value = "/love/chat/sse/event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppEvent(String userMessage, String chatId) {
        return loveApp.chatWithStream(userMessage, chatId)
                .map(chunk -> 
                        ServerSentEvent.<String>builder()
                                .data(chunk)
                                .build()
                );
    }

    @GetMapping(value = "/love/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppEmitter(String userMessage, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L); // 3 分钟超时
        loveApp.chatWithStream(userMessage, chatId)
                .subscribe(chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },emitter::completeWithError,emitter::complete);
        return emitter;
    }
    
    @GetMapping(value = "/manus/chat")
    public SseEmitter doChatWithManus(String userMessage) {
        return manus.runStream(userMessage);
    }
}
