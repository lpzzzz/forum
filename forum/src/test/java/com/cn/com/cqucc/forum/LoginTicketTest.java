package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.LoginTicketMapper;
import com.cn.com.cqucc.forum.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Random;

@SpringBootTest
public class LoginTicketTest {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void loginTicketInsert() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(153);
        loginTicket.setTicket(new Random().nextInt(100) + "");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date());
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void loginTicketSelectAndUpdate() {
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket("72");
        System.out.println(loginTicket);
        loginTicketMapper.updateLoginTicket(loginTicket.getTicket(),1);
    }
}
