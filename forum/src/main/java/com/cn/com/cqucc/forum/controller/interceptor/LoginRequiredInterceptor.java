package com.cn.com.cqucc.forum.controller.interceptor;

import com.cn.com.cqucc.forum.annotation.LoginRequired;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 登录拦截器
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    /**
     * 需要在请求开始之前进行拦截
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        User user = hostHolder.getUser();

        //首先判断我们拦截的类型是否属于方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && user == null) { // 我们访问了拦截的路径但是用户却未进行等录就可以将页面重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }

    /*在WebMvcConfig中配置上拦截器*/
}
