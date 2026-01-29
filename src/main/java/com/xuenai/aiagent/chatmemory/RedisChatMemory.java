package com.xuenai.aiagent.chatmemory;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的 ChatMemory
 */
@Component
public class RedisChatMemory implements ChatMemory {

    record ChatMemoryEntry(String messageType, String content) {
    };

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "chat:memory:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(7);


    @Override
    public void add(String conversationId, Message message) {
        String key = getKey(conversationId);
        ChatMemoryEntry entry = new ChatMemoryEntry(
                message.getMessageType().getValue(),
                message.getContent()
        );
        redisTemplate.opsForList().rightPush(key, entry);
        redisTemplate.expire(key, DEFAULT_TTL);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;
        String key = getKey(conversationId);
        List<ChatMemoryEntry> entries = messages.stream()
                .map(msg -> new ChatMemoryEntry(msg.getMessageType().getValue(), msg.getContent()))
                .toList();

        redisTemplate.opsForList().rightPushAll(key, entries.toArray());
        redisTemplate.expire(key, DEFAULT_TTL);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = getKey(conversationId);
        long start = lastN > 0 ? -lastN : 0;

        List<Object> results = redisTemplate.opsForList().range(key, start, -1);
        if (results == null) return new ArrayList<>();

        return results.stream()
                .map(this::mapToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(getKey(conversationId));
    }

    private Message mapToMessage(Object obj) {
        String type = "USER";
        String content = "";

        // Jackson 反序列化 Generic 类型时，通常会变成 LinkedHashMap
        if (obj instanceof Map<?, ?> map) {
            type = (String) map.get("messageType");
            content = (String) map.get("content");
        } else if (obj instanceof ChatMemoryEntry(String messageType, String content1)) {
            type = messageType;
            content = content1;
        }

        // 根据 type 还原对象
        if (MessageType.ASSISTANT.getValue().equalsIgnoreCase(type)) {
            return new AssistantMessage(content);
        } else if (MessageType.SYSTEM.getValue().equalsIgnoreCase(type)) {
            return new SystemMessage(content);
        } else {
            return new UserMessage(content);
        }
    }

    /**
     * 获取 Redis 的 key
     *
     * @param conversationId 对话 ID
     * @return Redis 的 key
     */
    private String getKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
