package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.Comment;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.entity.Event;
import com.cn.com.cqucc.forum.event.EventProducer;
import com.cn.com.cqucc.forum.service.CommentService;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.HostHolder;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 评论功能的表现层
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements ForumConstant {

    @Autowired
    private CommentService commentService;

    // 用于获取当前用户信息
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论信息
     *
     * @param discussId 需要根据帖子的id进行进一步的修改帖子的评论数量
     * @param comment   在评论中需要隐含包含 评论的类型 以及类型id
     * @return
     */
    @RequestMapping(path = "/addComment/{discussId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussId") String discussId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);// 默认是有效的
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        if (hostHolder.getUser() != null) {
            Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(comment.getEntityId())
                    .setEntityType(comment.getEntityType()).
                            setData("postId", discussId); // 我们点赞的帖子 用户可以进行查看
            //由于我们不知道评论的是什么所以需要 进行判断 是对帖子的评论还是对 评论的评论
            if (comment.getEntityType() == ENTITY_TYPE_POST) { // 如果是对帖子的评论
                DiscussPost target = discussPostService.selectDiscussPost(comment.getEntityId());// 查询对帖子的评论 评论过的帖子
                event.setEntityUserId((Integer.parseInt(target.getUserId())));
            } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) { // 如果是对评论的评论
                Comment target = commentService.selectCommentById(comment.getEntityId()); // 目标评论 评论过的评论
                event.setEntityUserId(target.getUserId());

            }
            eventProducer.fireEvent(event);

            // 只有我们在回帖的时候 才发布（触发事件）
            if (comment.getEntityType() == ENTITY_TYPE_POST) {
                event = new Event()
                        .setTopic(TOPIC_PUBLISH)
                        .setUserId(hostHolder.getUser().getId())
                        .setEntityId(Integer.valueOf(discussId))
                        .setEntityType(ENTITY_TYPE_POST);
                eventProducer.fireEvent(event); // 触发事件
                // 在有用户回帖的时候计算分数
                String postScoreKey = RedisUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(postScoreKey, Integer.valueOf(discussId));
            }
        }
        // 触发评论事件
        return "redirect:/discussDetail/" + discussId;
    }
}
