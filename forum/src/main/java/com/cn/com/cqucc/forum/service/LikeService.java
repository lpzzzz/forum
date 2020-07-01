package com.cn.com.cqucc.forum.service;

public interface LikeService {

    /**
     * 点赞
     * @param userId
     * @param entityType
     * @param entityId
     * @param entityUserId 别点赞实体的用户id
     */
    public void like(int userId, int entityType, int entityId, int entityUserId);

    /**
     * 统计每个实体获赞的数量
     *
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId);

    /**
     * 查询某用户的点赞状态
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return 返回值使用int 更加具备扩展性 可以有更多的状态
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId);

    /**
     * 查询某个用户被点赞的数量
     *
     * @param userId
     * @return
     */
    public int findUserLikedCount(int userId);
}
