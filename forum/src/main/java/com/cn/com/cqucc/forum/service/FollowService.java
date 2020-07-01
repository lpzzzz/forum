package com.cn.com.cqucc.forum.service;

import java.util.List;
import java.util.Map;

/**
 * 关注与取消关注业务层接口
 */
public interface FollowService {

    /**
     * 关注用户
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId);

    /**
     * 取消关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId, int entityType, int entityId);

    /**
     * 查询被关注者数量 关注实体的数量
     *
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId, int entityType);

    /**
     * 查询关注者数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType, int entityId);

    /**
     * 查询是否被关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId);


    /**
     * 查询某用户所关注的人
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> findFollowees(int userId,int offset,int limit);

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId,int offset,int limit);
}
