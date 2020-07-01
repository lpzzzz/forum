package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.MessageMapper;
import com.cn.com.cqucc.forum.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MessageTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessageMapper() {
        //查询会话列表
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);

        for (Message message : messages) {
            System.out.println(message);
        }

        // 查询会话数量
        int conversationCount = messageMapper.selectConversationCount(111);
        System.out.println("会话数量 ： " + conversationCount);

        // 查询私信列表
        List<Message> letters = messageMapper.selectLetters("111_112", 0, 10);

        for (Message letter : letters) {
            System.out.println(letter);
        }

        // 查询私信数量
        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println( "私信数量" + letterCount);
        int unreadMessage =messageMapper.selectLetterUnread(112,"111_112");
        System.out.println("未读消息数量" + unreadMessage);
    }

}
