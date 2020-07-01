package com.cn.com.cqucc.forum.dao;

import com.cn.com.cqucc.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据评论类型查询评论
     *
     * @param entityType 评论类型
     * @param entityId   类型id
     * @param offset     查询的起始位置
     * @param limit      每页显示条数
     * @return
     */
    List<Comment> selectCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据评论的类型查询评论
     *
     * @param entityType 评论的类型
     * @param entityId   类型的id
     * @return
     */
    int selectCommentCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    /**
     * 增加帖子
     *
     * @param comment
     * @return
     */
    int insertCommentByEntity(Comment comment);

    /**
     * 根据id查询评论
     *
     * @param id
     * @return
     */
    Comment selectCommentById(@Param("id") int id);

    /**
     * 根据实体类型和用户id查询评论
     *
     * @param entityType
     * @param userId
     * @return
     */
    List<Comment> selectCommentByEntityTypeAndUserId(@Param("entityType") int entityType, @Param("userId") int userId,@Param("offset") int offset, @Param("limit") int limit);

    int selectCommentCountByEntityTypeAndUserId(@Param("entityType") int entityType, @Param("userId") int userId);
}
