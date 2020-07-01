package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.util.ForumUtil;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping(path = "/http" ,method = RequestMethod.GET)
    @ResponseBody
    public void http(HttpServletRequest request , HttpServletResponse response) {
        System.out.println(request.getMethod());

        response.setContentType("text/html;charset=utf8");
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write("<h1>你好</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @RequestMapping(path = "/students" ,method = RequestMethod.GET)
    @ResponseBody
    public void getStudents(@RequestParam(name = "code", required = false,defaultValue = "01") String code,
                            @RequestParam(name = "name", required = false,defaultValue = "张三") String name) {
        System.out.println(code);
        System.out.println(name);
    }


    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public void getStudent(@PathVariable int id) {
        System.out.println(id);
    }


    @RequestMapping(path = "/saveStudent" ,method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name ,int age) {
        System.out.println(age);
        System.out.println(name);
        return "success";
    }

    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",23);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("number", ForumUtil.generateUUID());
        cookie.setPath("/forum/alpha/"); // 设置cookie的范围
        cookie.setMaxAge(60*10); // 设置超时时间
        response.addCookie(cookie);
        return "set cookie!";
    }

    @RequestMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("number") String cookie) {
        System.out.println(cookie);
        return "get cookie";
    }


    // session 演示

    @RequestMapping(path = "/session/set" , method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id",1);
        session.setAttribute("name","张三");
        return "set session";
    }

    @RequestMapping(path = "/session/get" , method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    // ajax示例
    @RequestMapping(path = "/ajax" , method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String code , String msg) { //写上需要接收的参数
        System.out.println(code);
        System.out.println(msg);
        String json = ForumUtil.getJSONString(code, msg);
        return json;
    }
}
