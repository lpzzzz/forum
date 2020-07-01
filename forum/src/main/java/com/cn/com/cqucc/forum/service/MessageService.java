package com.cn.com.cqucc.forum.service;

import com.cn.com.cqucc.forum.entity.Message;

import java.util.List;

/**
 * 会话私信业务层接口
 */
public interface MessageService {

    List<Message> selectConversations(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectLetters(String conversationId, int offset, int limit);

    int selectLetterCount(String conversationId);

    int selectUnreadMessage(int userId, String conversationId);

    int insertMessage(Message message);

    int updateMessageStatus(List<Integer> ids);

    int deleteMessage(List<Integer> ids);

    Message getLatestNotice(int userId , String topic);

    int selectNoticeCount(int userId , String topic);

    int selectUnreadNoticeCount(int userId , String topic);

    public List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
