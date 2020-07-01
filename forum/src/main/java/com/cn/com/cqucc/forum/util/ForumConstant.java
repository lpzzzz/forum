package com.cn.com.cqucc.forum.util;

/**
 * 激活状态 常量接口
 */
public interface ForumConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;


    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;


    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;


    /**
     * 默认状态下的登录超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 60 * 60 * 12;

    /**
     * 记住我状态下的登录超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 60 * 60 * 100;

    /**
     * 帖子评论常量
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 评论评论常量
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * User实体常量
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：发布帖子
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题：删除帖子
     */
    String TOPIC_DELETE = "delete";


    /**
     * 系统id
     */
    int SYSTEM_ID = 1;


    /**
     * 权限管理 ： 用户
     */
    String AUTHORITY_USER = "user";


    /**
     * 权限管理 ： 超级管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限管理 ： 版主
     */
    String AUTHORITY_MODERATOR = "moderator";

    /**
     * 主题 ： 分享 share
     */
    String TOPIC_SHARE = "share";
}
