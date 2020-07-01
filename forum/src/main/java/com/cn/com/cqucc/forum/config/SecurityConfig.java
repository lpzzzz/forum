package com.cn.com.cqucc.forum.config;

import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ForumConstant {


    /**
     * 用于忽略我们不用拦截的静态资源
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**"); // 忽略对静态资源的拦截
    }

    /**
     * 用于授权
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers( // 登录后才能访问的权限
                        "/user/setting",
                        "/addDiscussPost",
                        "/comment/addComment/**",
                        "/letter/**",
                        "/like",
                        "/follow ",
                        "/unfollow",
                        "/user/myPost",
                        "/user/myReply"
                )
                .hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR) // 三个中任意一个登录之后才能访问
                .antMatchers( // 版主权限
                        "/discussTop",
                        "/discussWonderful"
                )
                .hasAnyAuthority(AUTHORITY_MODERATOR, AUTHORITY_ADMIN)
                .antMatchers( // 超级管理员权限
                        "/discussDelete",
                        "/data/**",
                        "/actuator/**"
                )
                .hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll()// 其他的都是允许
                .and().csrf().disable(); // 禁用csrf
        // 权限不够的时候如何处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 未登录时的处理方式
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) { // 如果是异步请求 我们给出提示
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtil.getJSONString("403", "您还未登录哦!"));
                        } else { // 如果是普通请求 我们返回到登录页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 登录之后没有权限的时候如何处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) { // 如果是异步请求 我们给出提示
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtil.getJSONString("403", "你对该功能没有操作权限"));
                        } else { // 如果是普通请求 我们返回到404页面
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });


        // Security底层会默认拦截 /logOut请求，进行退出处理 我们可以覆盖它默认的逻辑
        http.logout().logoutUrl("/securityLogout");
    }
}
