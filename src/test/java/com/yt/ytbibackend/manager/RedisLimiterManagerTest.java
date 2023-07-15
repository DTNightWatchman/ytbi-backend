package com.yt.ytbibackend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;


    @Test
    void doRateLimit() {


        for (int i = 0; i < 100 ; i++) {
             redisLimiterManager.doRateLimit("1");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            System.out.println("成功" + ":" + i);
        }
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < 60; i++) {
//            boolean b = redisLimiterManager.doRateLimit(userId);
////            try {
////                Thread.sleep(1000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//            System.out.println("成功" + ":" + i + "-" + b);
//        }
    }
}