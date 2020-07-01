package com.cn.com.cqucc.forum.entity;

import lombok.Data;

import java.util.Date;

/**
 * 登录凭证
 */
@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
