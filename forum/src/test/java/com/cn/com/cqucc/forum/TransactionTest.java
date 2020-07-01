package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.service.impl.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransactionTest {

    @Autowired
    private AlphaService alphaService;

    /**
     * 测试事务控制
     */
    @Test
    public void testSaveUser(){
        Object o = alphaService.saveUser();
        System.out.println(o);
    }

    /**
     * 测试编程式事务控制
     */
    @Test
    public void testSave(){
        Object o = alphaService.save();
        System.out.println(o);
    }

}
