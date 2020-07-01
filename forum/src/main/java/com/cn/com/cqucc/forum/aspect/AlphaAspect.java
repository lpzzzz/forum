package com.cn.com.cqucc.forum.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/*@Component
@Aspect*/
public class AlphaAspect {
    //定义切入点表达式 第一个 * 表示任意的返回值 第二个 * 表示任意的类 第三个 * 表示任意的方法
    @Pointcut("execution(* com.cn.com.cqucc.forum.service.*.*(..))")
    public void pointCut() {

    }

    // 针对每一个业务方法都会触发 AOP

    /**
     * 前置通知 在方法的一开始记录日志
     */
    @Before("pointCut()")
    public void before() {
        System.out.println("before...........");
    }

    /**
     * 后置通知
     */
    @After("pointCut()")
    public void after() {
        System.out.println("after.............");
    }

    /**
     * 最终通知
     */
    @AfterReturning("pointCut()")
    public void afterReturning() {
        System.out.println("afterReturning.....");
    }

    /**
     * 异常通知
     */
    @AfterThrowing("pointCut()")
    public void afterThrowing() {
        System.out.println("afterThrow.........");
    }

    /**
     * 环绕通知可以处理 需要有但有返回值
     * @param joinPoint
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object proceed = joinPoint.proceed();
        System.out.println("around after");
        return proceed;
    }
}
