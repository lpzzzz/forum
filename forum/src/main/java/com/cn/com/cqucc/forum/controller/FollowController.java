package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.Event;
import com.cn.com.cqucc.forum.entity.Page;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.event.EventProducer;
import com.cn.com.cqucc.forum.service.FollowService;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements ForumConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) { // 当前的entityId一定是一个userId 因为 目前只有一种关注对象
        User user = hostHolder.getUser();

        if (user != null) {
            followService.follow(user.getId(), entityType, entityId);
            // 触发关注事件
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setEntityId(entityId)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityUserId(entityId);
            eventProducer.fireEvent(event);
            return ForumUtil.getJSONString("0", "已关注");
        }
        return ForumUtil.getJSONString("1", "用户尚未登录，请先登录!");
    }


    @RequestMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        if (user != null) {
            followService.unfollow(user.getId(), entityType, entityId);
            return ForumUtil.getJSONString("0", "已取消关注");
        }
        return ForumUtil.getJSONString("1", "用户尚未登录，请先登录!");
    }


    /**
     * 获取我关注的用户
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.selectById(userId);
        User loginUser = hostHolder.getUser();
        model.addAttribute("loginUser", loginUser);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user); // 回显我们选中的用户
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followeeList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        // 需要加入 关注的状态 遍历查看当前用户 在游客查看的时候是否已经关注
        if (followeeList != null) {
            for (Map<String, Object> followee : followeeList) {
                User u = (User) followee.get("user"); // 在关注列表中的用户
                followee.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followeeList", followeeList);
        return "site/followee";
    }

    /**
     * 获取我的粉丝
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.selectById(userId);
        User loginUser = hostHolder.getUser();
        model.addAttribute("loginUser", loginUser);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user); // 回显我们选中的用户
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> followerList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        // 需要加入 关注的状态 遍历查看当前用户 在游客查看的时候是否已经关注
        if (followerList != null) {
            for (Map<String, Object> follower : followerList) {
                User u = (User) follower.get("user"); // 在关注列表中的用户
                follower.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followerList", followerList);
        return "site/follower";
    }


    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) { // 如果为登录表示 未关注 返回false
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId); // 判断当前登录用户是否关注过 列表中的用户
    }
}
