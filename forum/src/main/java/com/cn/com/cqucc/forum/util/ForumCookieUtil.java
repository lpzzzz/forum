package com.cn.com.cqucc.forum.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ForumCookieUtil {

    public static String getCookie(HttpServletRequest request, String name) {

        if (request == null || name == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
