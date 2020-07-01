package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.*;
import com.cn.com.cqucc.forum.event.EventProducer;
import com.cn.com.cqucc.forum.service.*;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import com.cn.com.cqucc.forum.util.RedisUtil;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 帖子控制器类
 */
@Controller
public class DiscussPostController implements ForumConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 需要使用评论的业务方法
    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService; // 用于获取点赞数量

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 访问该路径就相当于访问index
     *
     * @return
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String root() {
        return "forward:/index";
    }

    @RequestMapping("/index")
    public String getDiscussPost(Model model, Page page,
                                 @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //方法在调用之前，SpringMvC会自动实例化Model和Page。并且将Page注入Model
        // 所以，在Thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.selectDiscussPostRows("0"));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts("0", page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> all = new ArrayList<>();

        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.selectById(Integer.parseInt(discussPost.getUserId()));
                // 查询帖子的点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
                map.put("user", user);
                all.add(map);
            }
        }

        // 输出测试
        for (Map<String, Object> one : all) {
            System.out.println(one);
        }

        model.addAttribute("all", all);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }

    @RequestMapping(path = "/addDiscussPost", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return ForumUtil.getJSONString("403", "您还未登录，请先登录!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId() + "");
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setType(0);
        post.setStatus(0);
        post.setCommentCount(0);
        post.setScore(0d);
        discussPostService.insertDiscussPost(post);

        // 发布帖子的时候发布事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(post.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event); // 触发事件

        // 发布帖子的时候 在redis中初始化帖子分数
        String postScoreKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        return ForumUtil.getJSONString("0", "发布成功!");
    }

    @RequestMapping(path = "/discussDetail/{discussId}", method = RequestMethod.GET)
    public String findDiscussPost(@PathVariable Integer discussId, Model model, Page page) {
        DiscussPost post = discussPostService.selectDiscussPost(discussId);
        model.addAttribute("post", post);
        //根据帖子的userId查询用户信息 有两种方式 ： 通过级联查询的方式（只用查询一次效率较高但是复杂）
        // 可以使用根据帖子的userId查询这样更加的方便 但是需要查询两次所以效率比较低，但是后面使用redis可以解决
        User user = userService.selectById(Integer.parseInt(post.getUserId()));
        model.addAttribute("user", user);

        // 处理评论的表现层 由于需要分页 需要在参数中加入page参数
        page.setLimit(5);
        page.setPath("/discussDetail/" + discussId);
        page.setRows(post.getCommentCount());

        List<Comment> comments = commentService.selectCommentByEntity
                (ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussId);
        // 点赞状态 判断登录状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussId);
        // 返回前端页面中
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);
        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 创建一个list来存储我们当前评论 以及当前评论所依赖的user信息
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                // 将评论的用户存入到map中
                commentVo.put("user", userService.selectById(comment.getUserId()));
                // 点赞数量
                long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("commentLikeCount", commentLikeCount);
                // 点赞状态
                int commentLikeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("commentLikeStatus", commentLikeStatus);
                // 查询回复列表
                List<Comment> replys = commentService.selectCommentByEntity
                        (ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                System.out.println(replys);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replys != null) {
                    for (Comment reply : replys) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.selectById(reply.getUserId()));
                        // 获取回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        //回复点赞数量
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        int replyLikeStatus = hostHolder.getUser() == null ? 0
                                : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("replyLikeCount", replyLikeCount);
                        replyVo.put("replyLikeStatus", replyLikeStatus);
                        // 将目标用户存入到map中
                        replyVo.put("target", target);
                        // 需要将map添加到 replyVoList中
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replyVoList", replyVoList);
                // 获取回复数量
                int replyCount = commentService.selectCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail"; // 注意：路径最前面不能加上 / 否则部署会出错
    }

    /**
     * 访问异常处理页面
     *
     * @return
     */
    // 异常处理请求 ： 因为我们处理完异常信息之后需要重定向到 错误页面所以我们需要增加一个请求错误页面的请求
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "error/500";
    }

    /**
     * 没有权限时候跳转的页面
     *
     * @return
     */
    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDenied() {
        return "error/404";
    }


    /**
     * 置顶帖子 版主
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/discussTop", method = RequestMethod.POST)
    @ResponseBody
    public String setTOp(int id) {
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            throw new RuntimeException("你的账户还未登录!");
        }
        discussPostService.updateDiscussPostType(id, 1);// 设置为1 置顶
        // 触发发帖事件将更新之后的帖子 存入到ES中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(loginUser.getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event); // 触发事件
        return ForumUtil.getJSONString("0"); // 成功!
    }


    /**
     * 帖子加精 版主
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/discussWonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            throw new RuntimeException("你的账户还未登录!");
        }
        discussPostService.updateDiscussPostStatus(id, 1);// 设置为1 置顶
        // 触发发帖事件将更新之后的帖子 存入到ES中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(loginUser.getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event); // 触发事件

        // 在加精帖子之后计算一次帖子的分数
        String postScoreKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return ForumUtil.getJSONString("0"); // 成功!
    }

    /**
     * 删除帖子：超级管理员
     *
     * @param id
     * @return
     */
    @RequestMapping(path = "/discussDelete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            throw new RuntimeException("你的账户还未登录!");
        }
        discussPostService.updateDiscussPostStatus(id, 2);// 设置为1 置顶
        // 触发发帖事件将更新之后的帖子 存入到ES中
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(loginUser.getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event); // 触发事件
        return ForumUtil.getJSONString("0"); // 成功!
    }

}
