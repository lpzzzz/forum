package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.mail.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSend() {
        mailClient.sendMail("2386258658@qq.com","测试邮件","我是测试邮件");
    }


    @Test
    public void testHtmlSend() {
        Context context = new Context();
        context.setVariable("username","lisi");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("14787005750@163.com","html",content);
    }

}
