package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.dao.MessageMapper;
import com.cn.com.cqucc.forum.entity.Message;
import com.cn.com.cqucc.forum.service.MessageService;
import com.cn.com.cqucc.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> selectConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    @Override
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> selectLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    @Override
    public int selectLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int selectUnreadMessage(int userId, String conversationId) {
        return messageMapper.selectLetterUnread(userId, conversationId);
    }

    @Override
    public int insertMessage(Message message) {
        //需要对私信内容进行过滤
        // 特殊标签过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public int updateMessageStatus(List<Integer> ids) {
        //将私信的状态设置为 1 已读
        return messageMapper.updateMessageStatus(ids, 1);
    }

    /**
     * 删除私信
     *
     * @param ids
     * @return
     */
    @Override
    public int deleteMessage(List<Integer> ids) {
        return messageMapper.updateMessageStatus(ids, 2);
    }


    @Override
    public Message getLatestNotice(int userId, String topic) {
        return messageMapper.getLatestNotice(userId, topic);
    }

    @Override
    public int selectNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    @Override
    public int selectUnreadNoticeCount(int userId, String topic) {
        return messageMapper.selectUnreadNoticeCount(userId, topic);
    }

    @Override
    public List<Message> selectNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
