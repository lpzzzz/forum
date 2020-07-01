package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.TimeZone;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    // 写两种请求方式的原因是方便后面使用转发 转发是在一次请求中 后面的请求使用的方式是POST
    public String getDataPage(Model model) {
        // 如何统计 今日的访客数
        // 获取今天0点即 24 点的访客数
        Long time = System.currentTimeMillis();  //当前时间的时间戳
        long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        // 计算 今天 0点 到目前的访客数
        long dailyUvCount = dataService.calculatorUv(new Date(zero), new Date());
        model.addAttribute("dailyUvCount", dailyUvCount);
        long dailyDauCount = dataService.calculatorDau(new Date(zero), new Date());
        model.addAttribute("dailyDauCount", dailyDauCount);
        return "site/admin/data";
    }

    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getDataUv(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, Model model) {
        long uvCount = dataService.calculatorUv(startDate, endDate);
        model.addAttribute("uvCount", uvCount);
        model.addAttribute("uvStartDate", startDate);
        model.addAttribute("uvEndDate", endDate);
        return "forward:/data"; //转发是一次请求 ，表示该请求未能完成全部任务需要继续处理 转发到的请求请求方式也必须是Post
    }

    /**
     * @param startDate 如果 请求中提交的是Date对象需要 使用 @DateTimeFormat(pattern = "yyyy-MM-dd") 注解指定提交Date对象的格式
     * @param endDate
     * @param model
     * @return
     */
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDataDau(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, Model model) {
        long dauCount = dataService.calculatorDau(startDate, endDate);
        model.addAttribute("dauCount", dauCount);
        model.addAttribute("dauStartDate", startDate);
        model.addAttribute("dauEndDate", endDate);
        return "forward:/data"; //转发是一次请求 ，表示该请求未能完成全部任务需要继续处理 转发到的请求请求方式也必须是Post
    }
}
