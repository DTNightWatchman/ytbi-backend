package com.yt.ytbibackend.utils;

import cn.hutool.extra.mail.MailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName MailUtils
 * @Description 邮箱测试
 * @Author Ricardo
 * @Date 2023/7/28/028 16:34
 */
@SpringBootTest
public class MailUtils {

    String MailTemplate =

    public static void main(String[] args) {
        MailUtil.send("ytbaiduren@gmail.com", "测试", "<h1>邮件来自Hutool测试</h1>", true);

        //MailUtil.send("ytbaiduren@gmail.com", "测试", "邮件来自Hutool测试", false);

    }
}
