package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.annotation.LoginRequired;
import com.cn.com.cqucc.forum.entity.Comment;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.entity.Page;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.*;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.RuntimeMBeanException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${forum.path.domain}")
    private String domain;

    @Autowired
    private HostHolder hostHolder; // 用于获取当前已登录的用户信息

    @Autowired
    private LikeService likeService; // 用于获取用户当前被点赞数量

    @Autowired
    private FollowService followService;

    @Autowired
    private CommentService commentService; // 获取我的回复

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerUrl;

    @Autowired
    private DiscussPostService discussPostService; //用于获取个人帖子

    /**
     * 实现文件上传云服务器的逻辑
     *
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 生成随机文件名称
        String fileName = ForumUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", ForumUtil.getJSONString("0"));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "site/setting";
    }


    @RequestMapping(path = "/header/updateUrl", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return ForumUtil.getJSONString("1", "上传头像的文件名称不能为空!");
        }
        // 拼接 七牛云 图片的访问路径
        String url = headerUrl + "/" + fileName;
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            return ForumUtil.getJSONString("1", "您还未登录还不能上传头像!");
        }
        System.out.println(url);
        userService.updateUserHeaderUrl(loginUser.getId() + "", url);
        return ForumUtil.getJSONString("0");
    }

    /**
     * 已废弃
     *
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {

        if (headerImage == null) {
            model.addAttribute("error", "您还未选择你所上传的头像!");
            return "site/setting";
        }
        // 获取上传文件的原始文件名称
        String filename = headerImage.getOriginalFilename();
        // 截取上传文件的后缀 表示从最后一个点之后截取
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确!");
            return "site/setting";
        }

        // 生成随机文件名称
        filename = ForumUtil.generateUUID() + suffix;
        File filePath = new File(uploadPath);

        //如果没有这个路径创建该路径
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        // 确定文件路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败!", e.getMessage());
            // 抛出一个异常打断整个程序 ， 后面统一处理Controller的异常
            throw new RuntimeException("上传文件文件失败服务器发生异常!", e);
        }

        // 在HostHolder中获取当前等录的用户 修改用户头像的路径
        User user = hostHolder.getUser();

        // 修改之后的路径是 : http://localost:9999/forum/user/header/xxx.png
        if (user != null) {
            String headerUrl = domain + contextPath + "/user/header/" + filename;
            userService.updateUserHeader(user.getId(), headerUrl);
        }
        return "redirect:/index";
    }


    /**
     * 已废弃
     *
     * @param response
     * @param filename
     */
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void header(HttpServletResponse response, @PathVariable("filename") String filename) {
        // 服务器图片存放路径
        filename = uploadPath + "/" + filename; // 图片存放路径
//        filename = uploadPath + "\\" + filename; // 图片存放路径
        // 获取文件后缀名用于 设置返回类型设置
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        // 响应图片
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename)
        ) {
            byte[] buffer = new byte[1024];// 定义缓冲区
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取头像失败!");
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updateUserPassword(@CookieValue("ticket") String ticket, String password, String newPassword, Model model) {
        User user = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("请先登录用户!");
        } else {
            Map<String, Object> map = userService.updateUserPassword(user.getId() + "", password, newPassword);
            if (map.get("passwordMsg") != null) {
                model.addAttribute("passwordMsg", map.get("passwordMsg").toString());
                return "site/setting";
            }

            if (map.get("newPasswordMsg") != null) {
                model.addAttribute("newPasswordMsg", map.get("newPasswordMsg").toString());
                return "site/setting";
            }
            // 退出等录 重新登录
            userService.exit(ticket);
            return "redirect:/login"; // 重定向到登录页面
        }
    }


    /**
     * 获取个人帖子列表
     *
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/myPost", method = RequestMethod.GET)
    public String getMyPostPage(Model model, Page page) {
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            throw new RuntimeException("您还未登录!");
        }
        model.addAttribute("flag", 1);//标识是否被选中
        model.addAttribute("loginUser", loginUser); // 将登录用户信息传递会前端页面

        // 获取该用户发帖数量
        int postCount = discussPostService.selectDiscussPostRows(loginUser.getId() + "");
        model.addAttribute("postCount", postCount);
        page.setRows(postCount);
        page.setPath("/user/myPost");
        page.setLimit(5);
        List<DiscussPost> discussPosts =
                discussPostService.selectDiscussPosts(loginUser.getId() + "", page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussPostList = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPostList.add(map);
            }
        }
        model.addAttribute("discussPostList", discussPostList);
        return "site/my-post";
    }

    /**
     * 获取我的回复
     *
     * @return
     */
    @RequestMapping("/myReply")
    public String getMyReply(Model model, Page page) {
        User loginUser = hostHolder.getUser();

        if (loginUser == null) {
            throw new RuntimeException("您还未登录!");
        }
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("flag", 2);//标识是否被选中
        int replyCount = commentService.selectCommentCountByEntityTypeAndUserId(ENTITY_TYPE_POST, loginUser.getId());
        model.addAttribute("replyCount", replyCount);
        page.setRows(replyCount);
        page.setPath("/user/myReply");
        page.setLimit(5);

        List<Comment> comments = commentService.selectCommentByEntityTypeAndUserId
                (ENTITY_TYPE_POST, loginUser.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                map.put("post", discussPostService.selectDiscussPost(comment.getEntityId()));
                commentList.add(map);
            }
        }
        model.addAttribute("commentList", commentList);
        return "site/my-reply";
    }


    @RequestMapping("/profile/{id}")
    public String getUserProfile(@PathVariable("id") String id, Model model) {
        // 个人主页需要展示个人信息需要将 个人信心根据id进行查询
        User user = userService.selectById(Integer.parseInt(id));
        if (user == null) {
            throw new RuntimeException("用户不存在,请勿恶意输入");
        }
        // 查询点赞数量
        int userLikedCount = likeService.findUserLikedCount(Integer.parseInt(id));
        model.addAttribute("flag", 3);//标识是否被选中
        model.addAttribute("user", user);
        model.addAttribute("userLikedCount", userLikedCount);

        // 关注与被关注
        // 查询被关注者数量 关注数量 被我关注的数量 我关注的人数量
        long followeeCount = followService.findFolloweeCount(Integer.parseInt(id), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, Integer.parseInt(id));
        model.addAttribute("followerCount", followerCount);

        // 是否已关注
        boolean hasFollow = false; // 如果我们登录也可以查看别人的个人信息

        if (hostHolder.getUser() != null) {
            hasFollow = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, Integer.parseInt(id));
            // 将登录用户返回给页面
            model.addAttribute("loginUser", hostHolder.getUser());
        }

        model.addAttribute("hasFollow", hasFollow);

        return "site/profile";
    }
}
