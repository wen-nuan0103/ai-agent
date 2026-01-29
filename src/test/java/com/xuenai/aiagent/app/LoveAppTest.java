package com.xuenai.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    void doChatWithRag() {
        String message = "有没有女朋友可以介绍一下，我 24岁，没有太多要求，喜欢二次元，最好在江浙沪";
        String answer =  loveApp.chatWithRag(message);
        System.out.println(answer);
    }

}