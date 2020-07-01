package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.dao.CommentMapper;
import com.cn.com.cqucc.forum.entity.Comment;
import com.cn.com.cqucc.forum.service.CommentService;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, ForumConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;


    /**
     * 根据评论类型查询评论
     *
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 根据评论类型查询评论评论数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public int selectCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public int insertComment(Comment comment) {

        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // html标签过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertCommentByEntity(comment);

        //更新评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCommentCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateDiscussPostCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    @Override
    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    @Override
    public List<Comment> selectCommentByEntityTypeAndUserId(int entityType, int userId, int offset, int limit) {
        return commentMapper.selectCommentByEntityTypeAndUserId(entityType, userId, offset, limit);
    }

    @Override
    public int selectCommentCountByEntityTypeAndUserId(int entityType, int userId) {
        return commentMapper.selectCommentCountByEntityTypeAndUserId(entityType, userId);
    }
}
