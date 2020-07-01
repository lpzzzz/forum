package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.FollowService;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, ForumConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType); // 被关注的目标
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType); //

                operations.multi(); // 开启事务
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis()); //被关注者 分数是当前时间的毫秒值
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis()); // 关注者
                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType); // 被关注的目标
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType); //

                operations.multi(); // 开启事务
                operations.opsForZSet().remove(followeeKey, entityId); //被关注者 分数是当前时间的毫秒值
                operations.opsForZSet().remove(followerKey, userId); // 关注者
                return operations.exec();
            }
        });
    }


    /**
     * 查询被关注者数量
     *
     * @param userId
     * @param entityType
     * @return
     */
    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询关注者数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询该用户是否被关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        Double score = redisTemplate.opsForZSet().score(followeeKey, entityId); // 在被关注中查询是否有该键值对
        return score != null; // 不为空表示已经被关注了
    }


    /**
     * 查询某用户关注的人
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        List<Map<String, Object>> followeeList = new ArrayList<>();
        // offset + limit - 1 查询到什么位置
        Set<Integer> followeesSet = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (followeesSet == null) {
            return null;
        }

        for (Integer followeeId : followeesSet) {
            Map<String, Object> map = new HashMap<>();
            // 查询出来的是关注者的id 根据id在数据库中查询 用户实体
            User user = userService.selectById(followeeId);
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, followeeId);
            map.put("followTime", new Date(score.longValue()));
            followeeList.add(map);
        }
        return followeeList;
    }

    /**
     * 查询某用户的粉丝
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        List<Map<String, Object>> followerList = new ArrayList<>();
        // offset + limit - 1 查询到什么位置 调用range 和 reverseRange
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            // 查询出来的是关注者的id 根据id在数据库中查询 用户实体
            User user = userService.selectById(targetId);
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            followerList.add(map);
        }
        return followerList;
    }
}
