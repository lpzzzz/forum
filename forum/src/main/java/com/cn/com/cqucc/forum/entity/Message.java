package com.cn.com.cqucc.forum.entity;

import lombok.Data;

import java.util.Date;

/**
 * 私信实体类
 */
@Data
public class Message {
    private Integer id;
    private Integer fromId; // 发送者id
    private Integer toId; // 接收者id
    private String conversationId; // 会话id
    private String content;
    private Integer status; // 私信状态 0-未读 1- 已读 2- 删除
    private Date createTime;
}
