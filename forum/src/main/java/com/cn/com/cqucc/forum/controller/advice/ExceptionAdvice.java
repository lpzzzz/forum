package com.cn.com.cqucc.forum.controller.advice;

import com.cn.com.cqucc.forum.util.ForumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异常处理通知类
 * <p>
 * 不需要在任何一个Controller上面加代码就能够实现对异常的统一处理
 */
@ControllerAdvice(annotations = Controller.class) // 只扫描Controller注解标识
public class ExceptionAdvice {

    //声明Logger记录日志
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class}) // 出现异常就记录日志
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：", e.getMessage());
        e.printStackTrace();
        // 记录异常栈
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            logger.error(stackTraceElement.toString());
        }

        // 需要判断请求的方式是 异步的请求还是 同步的请求
        String xRequestedWith = request.getHeader("x-requested-with");

        if ("XMLHttpRequest".equals(xRequestedWith)) { // 如果是异步请求
            // 这是一个异步请求
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(ForumUtil.getJSONString("1", "服务器异常!"));
        } else {
            // 普通请求需要重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
