package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.Event;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.event.EventProducer;
import com.cn.com.cqucc.forum.service.LikeService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements ForumConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int discussPostId) {
        User user = hostHolder.getUser();
        // 用于封装获取到的数据 方便进行返回
        Map<String, Object> map = new HashMap<>();

        if (user != null) {
            // 点赞
            likeService.like(user.getId(), entityType, entityId, entityUserId);
            // 计算实体被点赞数量E
            long likeCount = likeService.findEntityLikeCount(entityType, entityId);
            int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
            map.put("likeCount", likeCount);
            map.put("likeStatus", likeStatus);

            // 触发点赞事件 只有当用户点赞的时候 才进行通知 取消点赞的时候不需要进行通知
            if (likeStatus == 1 && hostHolder.getUser() != null) { // 且当前用户为登录状态
                Event event = new Event()
                        .setTopic(TOPIC_LIKE)
                        .setEntityType(entityType)
                        .setEntityId(entityId)
                        .setUserId(hostHolder.getUser().getId())
                        .setEntityUserId(entityUserId)
                        .setData("postId", discussPostId);
                eventProducer.fireEvent(event);
            }

            // 设置条件在给帖子点赞的时候计算分数
            if (entityType == ENTITY_TYPE_POST) {
                String postScoreKey = RedisUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(postScoreKey, discussPostId);
            }

            // 点赞与发送通知 并发执行
        }

        return ForumUtil.getJSONString("0", null, map);

    }
}
