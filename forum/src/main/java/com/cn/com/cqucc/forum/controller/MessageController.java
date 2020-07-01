package com.cn.com.cqucc.forum.controller;

import com.alibaba.fastjson.JSONObject;
import com.cn.com.cqucc.forum.entity.Message;
import com.cn.com.cqucc.forum.entity.Page;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.MessageService;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/letter")
public class MessageController implements ForumConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/conversationList", method = RequestMethod.GET)
    public String getConversation(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setPath("/letter/conversationList");
        page.setLimit(5);
        /*设置的是会话的总条数*/
        page.setRows(messageService.selectConversationCount(user.getId()));

        // 获取会话列表
        List<Message> conversations = messageService.selectConversations(
                user.getId(), page.getOffset(), page.getLimit());

        List<Map<String, Object>> conversationList = new ArrayList<>();

        if (conversations != null) {
            for (Message conversation : conversations) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", conversation);
                /*每个私信的会话数量*/
                map.put("letterCount", messageService.selectLetterCount(conversation.getConversationId()));
                //未读消息数量
                map.put("unreadCount",
                        messageService.selectUnreadMessage(user.getId(), conversation.getConversationId()));
                int target = user.getId() ==
                        conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                // 通过我们的目标id 查询会话中应该显示的用户
                User targetUser = userService.selectById(target);
                map.put("targetUser", targetUser);
                //每次循环添加一次
                conversationList.add(map);
            }
        }
        model.addAttribute("conversationList", conversationList);
        // 查询未读消息数量
        model.addAttribute(
                "conversationCount", messageService.selectUnreadMessage(user.getId(), null));
        int noticeUnreadCount = messageService.selectUnreadNoticeCount(hostHolder.getUser().getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "site/letter";
    }

    @RequestMapping(path = "/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        page.setRows(messageService.selectLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);
        page.setLimit(5);

        List<Message> letters = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterList = new ArrayList<>();
        if (letters != null) {
            for (Message letter : letters) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                // 将接受者id转换为接受者user对象存入map
                map.put("fromUser", userService.selectById(letter.getFromId()));
                letterList.add(map);
            }
        }

        model.addAttribute("letterList", letterList);
        // 需要的还有发送者对象
        model.addAttribute("targetUser", getTargetUser(conversationId));
        model.addAttribute("conversationId", conversationId);
        getUnreadMessageListId(letters);
        return "site/letter-detail";
    }

    private void getUnreadMessageListId(List<Message> letters) {
        //访问私信详情后需要将私信设置为 已读状态
        List<Integer> ids = new ArrayList<>();
        // 需要将私信列表里面有未读的标识为已读
        if (letters != null) {
            for (Message letter : letters) {
                if (letter.getStatus() == 0 && (hostHolder.getUser().getId().equals(letter.getToId()))) { // 只有当前的用户是接受者才能判断为已读
                    ids.add(letter.getId());
                }
            }
        }
        if (!ids.isEmpty()) {// 判断集合不为空
            messageService.updateMessageStatus(ids);
        }
    }

    /**
     * 通过conversationId 与 登录用户 id 进行判断 当前的用户时接收者id还是发送者id
     *
     * @param conversationId
     * @return
     */
    private User getTargetUser(String conversationId) {
        String[] id = conversationId.split("_");
        int id1 = Integer.parseInt(id[0]);
        int id0 = Integer.parseInt(id[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.selectById(id1);
        } else {
            return userService.selectById(id0);
        }
    }

    @RequestMapping(path = "/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 根据用户名查询用户
        User targetUser = userService.selectByUserName(toName);

        if (targetUser == null) {
            return ForumUtil.getJSONString("1", "你选择的用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        message.setStatus(0);
        message.setContent(content);
        message.setCreateTime(new Date());
        // 设置 会话id 需要将id jin进行拼接 小的数在后面
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        System.out.println(message);
        messageService.insertMessage(message);
        return ForumUtil.getJSONString("0");
    }


    @RequestMapping(path = "/deleteMessage/{id}/{conversationId}", method = RequestMethod.GET)
    public String deleteMessage(@PathVariable("id") Integer id, @PathVariable("conversationId") String conversationId) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        messageService.deleteMessage(ids);
        return "redirect:/letter/detail/" + conversationId;
    }


    @RequestMapping(path = "/noticeList", method = RequestMethod.GET)
    public String getNoticeList(Model model) {


        if (hostHolder.getUser() != null) {
            //评论消息通知
            Message message = messageService.getLatestNotice(hostHolder.getUser().getId(), TOPIC_COMMENT);
            if (message != null) {
                Map<String, Object> messageVo = new HashMap<>();
                messageVo.put("message", message);
                // 需要对 消息内容进行转换
                String content = HtmlUtils.htmlUnescape(message.getContent());
                if (content != null) {
                    Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                    messageVo.put("user", userService.selectById((Integer) data.get("userId")));
                    messageVo.put("entityId", data.get("entityId"));
                    messageVo.put("entityType", data.get("entityType"));
                    messageVo.put("postId", data.get("postId"));

                    // 获取到评论消息的数量
                    int count = messageService.selectNoticeCount(hostHolder.getUser().getId(), TOPIC_COMMENT);
                    messageVo.put("count", count);
                    int unread = messageService.selectUnreadNoticeCount(hostHolder.getUser().getId(), TOPIC_COMMENT);
                    messageVo.put("unread", unread);
                }
                model.addAttribute("commentNotice", messageVo);
            }


            //关注消息通知
            message = messageService.getLatestNotice(hostHolder.getUser().getId(), TOPIC_FOLLOW);
            if (message != null) {
                Map<String, Object> messageVo = new HashMap<>();
                messageVo.put("message", message);
                // 需要对 消息内容进行转换
                String content = HtmlUtils.htmlUnescape(message.getContent());
                if (content != null) {
                    Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                    messageVo.put("user", userService.selectById((Integer) data.get("userId")));
                    messageVo.put("entityId", data.get("entityId"));
                    messageVo.put("entityType", data.get("entityType"));
                    messageVo.put("postId", data.get("postId"));

                    // 获取到评论消息的数量
                    int count = messageService.selectNoticeCount(hostHolder.getUser().getId(), TOPIC_FOLLOW);
                    messageVo.put("count", count);

                    int unread = messageService.selectUnreadNoticeCount(hostHolder.getUser().getId(), TOPIC_FOLLOW);
                    messageVo.put("unread", unread);
                }
                model.addAttribute("followNotice", messageVo);
            }

            //关注消息通知
            message = messageService.getLatestNotice(hostHolder.getUser().getId(), TOPIC_LIKE);
            if (message != null) {
                Map<String, Object> messageVo = new HashMap<>();
                messageVo.put("message", message);
                // 需要对 消息内容进行转换
                String content = HtmlUtils.htmlUnescape(message.getContent());
                if (content != null) {
                    Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                    messageVo.put("user", userService.selectById((Integer) data.get("userId")));
                    messageVo.put("entityId", data.get("entityId"));
                    messageVo.put("entityType", data.get("entityType"));

                    // 获取到评论消息的数量
                    int count = messageService.selectNoticeCount(hostHolder.getUser().getId(), TOPIC_LIKE);
                    messageVo.put("count", count);

                    int unread = messageService.selectUnreadNoticeCount(hostHolder.getUser().getId(), TOPIC_LIKE);
                    messageVo.put("unread", unread);
                }
                model.addAttribute("likeNotice", messageVo);
            }


            // 查询所有的未读消息数量 私信的所有未读消息数量 + 系统消息的未读消息数量
            int letterUnreadCount = messageService.selectUnreadMessage(hostHolder.getUser().getId(), null);
            model.addAttribute("letterUnreadCount", letterUnreadCount);
            int noticeUnreadCount = messageService.selectUnreadNoticeCount(hostHolder.getUser().getId(), null);
            model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        }
        return "site/notice";
    }


    @RequestMapping(path = "/noticeDetail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        // 查询登录用户 也就是当前用户的登录状态
        User loginUser = hostHolder.getUser();
        if (loginUser == null) {
            throw new RuntimeException("您当前还未登录!");
        }

        page.setLimit(5);
        page.setPath("/letter/noticeDetail/" + topic);
        page.setRows(messageService.selectNoticeCount(loginUser.getId(), topic));
        // 查询 通知列表
        List<Message> noticeList = messageService.selectNotices(loginUser.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();

        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                // 对内容格式进行转换
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.selectById((Integer) data.get("userId")));
                map.put("entityId", data.get("entityId"));
                map.put("entityType", data.get("entityType"));
                map.put("postId", data.get("postId"));
                // 通知发送的作者
                map.put("fromUser", userService.selectById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }

        model.addAttribute("notices", noticeVoList);
        //设置消息为已读
        getUnreadMessageListId(noticeList);
        return "site/notice-detail";
    }
}