package com.cn.com.cqucc.forum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.TimeZone;

@SpringBootTest
public class DateTest {

    /**
     * 可以计算当天00 点 的时间
     */
    @Test
    public void testZero() {
        Long  time = System.currentTimeMillis();  //当前时间的时间戳
        long zero = time/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
        System.out.println(new Date(zero));
    }
}
