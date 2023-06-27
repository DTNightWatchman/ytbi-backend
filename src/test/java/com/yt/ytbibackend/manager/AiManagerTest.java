package com.yt.ytbibackend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String content = aiManager.doChat(1671934662349438978L, "分析需求:\n" +
                "分析用户的增长情况,请使用堆叠图\n" +
                "原始数据:\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30\n" +
                "4号,40\n" +
                "5号,50\n" +
                "6号,60\n" +
                "7号,0\n" +
                "8号,80\n" +
                "9号,90\n" +
                "10号,190\n" +
                "11号,110\n" +
                "12号,120");
        System.out.println(content);
    }

    @Test
    void testDoChat() {
    }
}