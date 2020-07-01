package com.cn.com.cqucc.forum.entity;

import lombok.Data;

import java.util.Date;

/**
 * 评论实体类
 */
@Data
public class Comment {
    private Integer id; // 评论Id
    private Integer userId; // 评论用户的id
    private Integer entityType; // 评论的类型， 是什么的评论是帖子的评论还是评论的评论
    private Integer entityId; //
    private Integer targetId; // 目标id 主要是评论下回复的id
    private String content; // 评论内容
    private Integer status; // 评论状态
    private Date  createTime;
}
