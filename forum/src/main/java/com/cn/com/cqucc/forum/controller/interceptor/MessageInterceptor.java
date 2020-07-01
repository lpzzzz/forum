package com.cn.com.cqucc.forum.controller.interceptor;

import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.MessageService;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    /**
     * 什么时候去查 ：调用Controller 之后模板之前
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User loginUser = hostHolder.getUser();
        if (loginUser != null && modelAndView != null) {
            int unreadNoticeAllCount = messageService.selectUnreadNoticeCount(loginUser.getId(), null);
            int unreadLetterAllCount = messageService.selectUnreadMessage(loginUser.getId(), null);
            modelAndView.addObject("allUnreadCount", unreadLetterAllCount + unreadNoticeAllCount);
        }
    }
}
