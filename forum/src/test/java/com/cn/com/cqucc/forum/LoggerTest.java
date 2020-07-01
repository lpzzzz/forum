package com.cn.com.cqucc.forum;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 设置日志级别并将日志输出到终端
 */

@SpringBootTest
public class LoggerTest {


    @Test
    public void testLogger() {
        Logger logger = LoggerFactory.getLogger(LoggerTest.class);
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }
}
