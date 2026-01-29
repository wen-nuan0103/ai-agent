package com.xuenai.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {
    
    @Resource
    private LoveApp loveApp;

    @Test
    void chat() {
        String answer = loveApp.chat("你好");
        System.out.println(answer);
        answer = loveApp.chat("我的女朋友叫鞠婧祎");
        System.out.println(answer);
        answer = loveApp.chat("请问我的女朋友名字叫什么？注意只需要回答我刚才说了什么，直接告诉我名字");
        System.out.println(answer);

    }
}