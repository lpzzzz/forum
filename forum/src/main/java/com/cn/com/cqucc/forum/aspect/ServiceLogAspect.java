package com.cn.com.cqucc.forum.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 业务层记录日志切面
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 配置切入点
    @Pointcut("execution(* com.cn.com.cqucc.forum.service.*.*(..))")
    public void pointCut() {
    }

    /**
     * 在方法执行之前记录日志
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4]在[yyyy-MM-dd HH:mm:ss] 访问了某方法
        // 获取Request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 通过消费者调用的 是不通过Controller 没有request

        if (attributes == null) { // 如果是空 则不再记日志
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //获取访问对象的ip地址
        String ip = request.getRemoteHost();
        // 获取访问时间并格式化
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 获取类名
        String targetMethodName = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();
        //因为未发生错误所以直接使用info进行记录
        logger.info(String.format("用户[%s]在[%s]访问了[%s]方法.", ip, now, targetMethodName));
    }

}
