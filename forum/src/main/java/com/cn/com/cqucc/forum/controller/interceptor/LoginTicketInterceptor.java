package com.cn.com.cqucc.forum.controller.interceptor;

import com.cn.com.cqucc.forum.entity.LoginTicket;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumCookieUtil;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 由于获取用户的登录信息是一个公共的功能所以需要将其放到拦截器中进行处理 减少操作
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {


    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 创建一个工具类用于获取到cookie中的数据

        // 从cookie中获取登录凭证
        String ticket = ForumCookieUtil.getCookie(request, "ticket");
        if (ticket != null) {
            // 根据ticket查询LoginTicket
            LoginTicket loginTicket = userService.selectLoginTicket(ticket);
            if (loginTicket != null) { // 易出现空指针异常 判断不为空才执行
                if (loginTicket.getTicket() != null
                        && loginTicket.getStatus() == 0
                        && loginTicket.getExpired().after(new Date())) { // 需要判断查询到的登录凭证是否存在且登录状态失效且未超时
                    // 根据登录凭证查询 用户的id
                    User user = userService.selectById(loginTicket.getUserId());
                    // 但是这个user对象应该存储到哪里去呢？ 本次请求持有的用户 ，但是浏览器与服务器是多对一的关系，存在高并发的情况所以我们需要对user对象特殊处理
                    hostHolder.setUser(user);
                    // 构建用户认证结果，并存入到SecurityContext 中，以便于Security进行授权
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user, user.getPassword(), userService.getAuthorities(user.getId()));
                    System.out.println(user.getUserName());
                    for (GrantedAuthority grantedAuthority : userService.getAuthorities(user.getId())) {
                        System.out.println(grantedAuthority.toString() + " +++++++++++++++++++++++ ");
                    }
                    SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 在模板引擎之前就需要使用user对象
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清理user对象
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }

    /*之后需要在WebMVCConfig中对拦截器进行配置*/
}
