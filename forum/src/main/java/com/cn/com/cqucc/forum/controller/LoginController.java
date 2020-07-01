package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.cn.com.cqucc.forum.util.RedisUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录注册
 */

@Controller
public class LoginController implements ForumConstant {


    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String context_path;

    @Autowired
    private RedisTemplate redisTemplate; // 用于将验证码存入到redis中去

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String register() {
        return "site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login() {
        return "site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，激活邮件已经发送到您的邮箱，请您尽快进行激活!");
            model.addAttribute("target", "/index"); // 我们的目标页面
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }


    //http://localhost:9999/forum/activation/101/code
    @RequestMapping(path = "/activation/{id}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("id") int id, @PathVariable("code") String code) {
        int activationCode = userService.activation(id, code);
        if (activationCode == ACTIVATION_SUCCESS) {
            // 激活成功！
            model.addAttribute("msg", "激活成功，请进行登录");
            model.addAttribute("target", "/login"); // 我们的目标页面
        } else if (activationCode == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "非法操作，你的账号已经激活，请勿重复操作！");
            model.addAttribute("target", "/index"); // 我们的目标页面
        } else {
            model.addAttribute("msg", "激活失败，你的激活码不正确");
            model.addAttribute("target", "/index"); // 我们的目标页面
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入到session中
        //session.setAttribute("kaptcha", text);
        // 将验证码存入到redis中去
        String kaptchaOwner = ForumUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);// 设置超时时间 为 60s
        cookie.setPath(context_path);
        response.addCookie(cookie); //将cookie发回到浏览器端标识用户
        String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);// 设置超时时间并设置单位为秒
        // 将图片输出到浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("响应验证码失败!", e.getMessage());
        }
    }

    @RequestMapping("/kaptcha/get")
    @ResponseBody
    public String getKaptcha(HttpSession session) {
        System.out.println(session.getAttribute("kaptcha"));
        return "get kaptcha";
    }


    @RequestMapping(path = "/login", method = RequestMethod.POST)
    // 如果参数是一个对象 SpringMVC会自动将其存入到Model中 但是如果是普通的形参 将不会存储到Model中但是这次请求未结束，参数的值会存在于request对象中，页面中可以通过param获取request对象中的参数值
    public String login(String userName, String password, String code,
                        boolean rememberMe,
                        HttpServletResponse response, Model model, @CookieValue("kaptchaOwner") String kaptchaOwner) {
//        String kaptcha = (String) session.getAttribute("kaptcha");
        // 构建验证码 key kaptchaKey
        String kaptcha = null;
        if (StringUtils.isNoneBlank(kaptchaOwner)) {// 如果验证码未过期
            String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "site/login";
        }
        // 检查账号、密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(userName, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());// map中的值存储的是一个对象需要将其转换为字符串
            cookie.setPath(context_path); // 在配置文件中获取
            cookie.setMaxAge(expiredSeconds); // 设置cookie的超时时间
            response.addCookie(cookie);
            return "redirect:/index"; // 如果在map中包含了登录凭证表示登录成功!
        } else {
            model.addAttribute("userNameMsg", map.get("userNameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 退出登录功能
     *
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/exit", method = RequestMethod.GET)
    public String exit(@CookieValue("ticket") String ticket) {
        userService.exit(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login"; //重定向默认的请求方式是get
    }

}
