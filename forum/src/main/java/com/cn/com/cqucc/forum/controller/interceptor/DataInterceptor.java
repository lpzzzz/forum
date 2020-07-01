package com.cn.com.cqucc.forum.controller.interceptor;

import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.DataService;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 网站数据统计 拦截器 拦截每一个请求 记录访问记录
 */

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 在Controller之前进行拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取访客的ip
        String ip = request.getRemoteHost();
        dataService.recordUv(ip);
        User user = hostHolder.getUser();
        if (user != null) { // 当用户登录的时候才记录
            dataService.recordDau(user.getId()); // 记录活跃用户
        }
        return true;
    }
}
