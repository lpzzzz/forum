package com.cn.com.cqucc.forum.service;

import com.cn.com.cqucc.forum.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentService {

    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    int selectCommentCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    /**
     * 根据实体类型和用户id查询评论
     *
     * @param entityType
     * @param userId
     * @return
     */
    List<Comment> selectCommentByEntityTypeAndUserId(int entityType, int userId, int offset ,int limit);

    int selectCommentCountByEntityTypeAndUserId(int entityType, int userId);
}
