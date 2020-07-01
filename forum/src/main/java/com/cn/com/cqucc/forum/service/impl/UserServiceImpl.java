package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.dao.UserMapper;
import com.cn.com.cqucc.forum.entity.LoginTicket;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.mail.MailClient;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, ForumConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

   /* @Autowired
    private LoginTicketMapper loginTicketMapper;*/


    @Value("${forum.path.domain}")
    private String domain;


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User selectById(Integer id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    @Override
    public User selectByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public int updateUserStatus(int id, int status) {
        int rows = userMapper.updateUserStatus(id, status);
        // 更新完成之后将原来的数据清除
        clearCache(id);
        return rows;
    }

    @Override
    public int updateUserHeader(int id, String headerUrl) {
        int rows = userMapper.updateUserHeader(id, headerUrl);
        clearCache(id); // 想进行数据库的更新之后再清除缓存，这样保证当我们清除
        return rows;
    }

    @Override
    public int updateUserPassword(int id, String password) {
        int rows = userMapper.updateUserPassword(id, password);
        clearCache(id);
        return rows;
    }


    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        if (StringUtils.isBlank(user.getUserName())) {
            map.put("usernameMsg", "用户名不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByUserName(user.getUserName());
        if (u != null) {
            map.put("usernameMsg", "该用户名已经被注册!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已经被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(ForumUtil.generateUUID().substring(0, 5));// 获取6位的salt
        user.setPassword(ForumUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0); //设置初次注册的用户类型为0普通用户
        user.setStatus(0); // 初次注册的账户为未激活状态
        user.setActivationCode(ForumUtil.generateUUID());//设置激活码
        // 乐动网 随机图片 http://images.nowcoder.com/head/1t.png
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png"
                , new Random().nextInt(1000))); // 设置随机头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //规定链接格式 http://localhost:9999/forum/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);
        return map;
    }

    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    @Override
    public int activation(Integer userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateUserStatus(userId, 1); // 更新状态码
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /***
     * 登录业务
     * @param userName
     * @param password
     * @param expiredSeconds
     * @return
     */
    @Override
    public Map<String, Object> login(String userName, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectByUserName(userName);

        if (StringUtils.isBlank(userName)) {
            map.put("userNameMsg", "用户名不能为空!");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        if (user == null) {
            map.put("userNameMsg", "该账号不存在!");
            return map;
        }

        // 验证激活状态
        if (user.getStatus() == 0) {
            map.put("userNameMsg", "该账号尚未激活请前往邮箱激活!");
            return map;
        }

        // 验证密码
        password = ForumUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码错误!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ForumUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + (expiredSeconds * 1000)));/*过期时间等于当前时间加上 过期限制*/
        // loginTicketMapper.insertLoginTicket(loginTicket);
        // 将登录凭证存入到 redis中
        String ticketKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);// 将登录的对象存入到redis中 这里 会将对象序列化为 json格式之后存入到redis
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     */
    @Override
    public void exit(String ticket) {
       /* LoginTicket loginTicket = loginTicketMapper.selectLoginTicket(ticket);
        loginTicketMapper.updateLoginTicket(loginTicket.getTicket(), 1);*/
        // 退出需要将 传递过来的 登录凭证 在redis中进行查询 查询到的是 登录对象 之后将登录状态修改之后再次存入到redis中
        String ticketKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
        clearCache(loginTicket.getUserId()); // 退出后清理用户缓存
    }

    /**
     * 根据cookie中的ticket查询登录凭证
     *
     * @param ticket
     * @return
     */
    @Override
    public LoginTicket selectLoginTicket(String ticket) {
        return (LoginTicket) redisTemplate.opsForValue().get(RedisUtil.getTicketKey(ticket));
    }


    /**
     * 上传文件之后修改用户头像的路径
     *
     * @param userId
     * @param headerUrl
     * @return
     */
    @Override
    public int updateUserHeaderUrl(String userId, String headerUrl) {
        int rows = userMapper.updateUserHeader(Integer.parseInt(userId), headerUrl);
        clearCache(Integer.parseInt(userId));
        return rows;
    }

    @Override
    public Map<String, Object> updateUserPassword(String userId, String password, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        if (password == null) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if (newPassword == null) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        User user = userMapper.selectById(Integer.parseInt(userId));
        password = ForumUtil.md5(password + user.getSalt()); // 这个是输入的密码
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "原密码不正确!");
            return map;
        }

        newPassword = ForumUtil.md5(newPassword + user.getSalt());
        // 更新密码
        userMapper.updateUserPassword(Integer.parseInt(userId), newPassword);
        clearCache(Integer.parseInt(userId));
        return map;
    }

    @Override
    public User getCache(int userId) {
        String userCacheKey = RedisUtil.getUserCacheKey(userId);
        return (User) redisTemplate.opsForValue().get(userCacheKey);
    }

    @Override
    public User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userCacheKey = RedisUtil.getUserCacheKey(userId);
        redisTemplate.opsForValue().set(userCacheKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    @Override
    public void clearCache(int userId) {
        String userCacheKey = RedisUtil.getUserCacheKey(userId);
        redisTemplate.delete(userCacheKey);
    }

    /**
     * 根据用户的id查询用户的权限
     *
     * @param userId
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.selectById(userId);

        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
