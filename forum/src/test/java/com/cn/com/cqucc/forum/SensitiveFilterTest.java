package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveFilterTest {
    @Autowired
    SensitiveFilter sensitiveFilter;

    /**
     * 敏感词果过滤测试
     */
    @Test
    public void SensitiveFilter() {
        String text = "张三♣赌♣博♣，这里可以♣赌♣博♣、♣嫖♣娼♣、可以♣开♣票♣、可以♣打♣架♣、可以♣约♣妹♣子♣。";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);
    }
}
