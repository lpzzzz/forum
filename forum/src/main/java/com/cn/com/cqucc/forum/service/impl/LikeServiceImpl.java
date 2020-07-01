package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.service.LikeService;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞与取消点赞 某个有实体类型与id组成的 键对应的集合中有 多个userId对应的就是我们所点赞的数量
     * 由于这里是 对redis的多次操作所以需要使用 事务处理
     * 当我们进行点赞的时候有两个操作 ： 1. 点赞记录一条点赞 取消点赞移除一条数据 2. 被点赞加一 取消点赞则减一
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @param entityUerId 该id是被点赞 的实体的 用户id 可由页面获得
     */
    @Override
    public void like(int userId, int entityType, int entityId, int entityUerId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisUtil.getUserLikeKey(entityUerId);

                // 判断当前 用户的点赞状态 根据检查查询redis中是否存在该条数据 查询操作需要在事务执行之前或之后执行
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                operations.multi(); // 开启事务

                if (isMember) { // 已经点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey); // 数字减一操作
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey); // 数字加一
                }

                return operations.exec();// 执行操作
            }
        });
    }

    /**
     * 查询实体的点赞数量
     *
     * @return
     */
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实体点赞的状态
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // 在redis中查询某人的值是否存在判断点赞状态
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * 查询某个用户被点赞的数量
     *
     * @param userId
     * @return
     */
    public int findUserLikedCount(int userId) {
        String userLikedKey = RedisUtil.getUserLikeKey(userId);
        Integer userLikedCount = (Integer) redisTemplate.opsForValue().get(userLikedKey); // 获取的值是一个对象 便是被点赞数量
        // 最后判断返回的数量是否是一个空对象 如果是 返回 0 否则返回这个数据的整数形式
        return userLikedCount == null ? 0 : userLikedCount.intValue();
    }
}
