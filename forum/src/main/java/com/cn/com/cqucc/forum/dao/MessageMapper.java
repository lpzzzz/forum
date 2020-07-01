package com.cn.com.cqucc.forum.dao;

import com.cn.com.cqucc.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 根据用户id查询会话列表
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据用户id查询会话数量
     *
     * @param userId
     * @return
     */
    int selectConversationCount(int userId);

    /**
     * 根据会话id查询私信列表
     *
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据会话id查询私信的数量
     *
     * @param conversionId
     * @return
     */
    int selectLetterCount(String conversionId);

    /**
     * 根据用户id 和 会话id查询未读私信的数量
     * 由于根据userId只能查询的是某一个用户私信的未读数量，如果需要查询某一个会话私信的未读数量需要加上会话的id
     *
     * @param userId
     * @param conversationId
     * @return
     */
    int selectLetterUnread(@Param("userId") int userId, @Param("conversationId") String conversationId);

    /**
     * 插入私信
     *
     * @param message
     * @return
     */
    int insertMessage(Message message);


    /**
     * 设置私信为已读状态
     *
     * @param ids    可以同时接收多个消息id
     * @param status
     * @return
     */
    int updateMessageStatus(@Param("ids") List<Integer> ids, @Param("status") int status);


    /**
     * 查询某一主题下最新的消息
     *
     * @param userId
     * @param topic
     * @return
     */
    Message getLatestNotice(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 查询某一主题下的消息数量
     *
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 查询某一主题下 未读消息的数量 或者 查询所有未读消息的数量 （当我们传递主题参数的时候
     * ）
     *
     * @param userId
     * @param topic
     * @return
     */
    int selectUnreadNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 查询某一用户 某一主题下的 通知详情
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> selectNotices(@Param("userId") int userId,  @Param("topic") String topic,  @Param("offset")int offset,  @Param("limit")int limit);
}
