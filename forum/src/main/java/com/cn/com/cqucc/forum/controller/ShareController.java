package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.Event;
import com.cn.com.cqucc.forum.event.EventProducer;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现生成长图并分享的功能
 */
@Controller
public class ShareController implements ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String saveImagePath; // 文件保存的路径

    @Value("${qiniu.bucket.share.url}")
    private String shareUrl;

    /**
     * 只需要传递当前我们需要进行分享的页面 路径就可以生成图片
     *
     * @param htmlUrl
     * @return
     */
    @RequestMapping("/share")
    @ResponseBody
    public String share(String htmlUrl) {
        // 避免文件名称重复 需要使用随机生成
        String fileName = ForumUtil.generateUUID();

        // 异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)  // 需要生成长图的网址
                .setData("fileName", fileName)  // 生成图片后的文件名称
                .setData("suffix", ".png");  // 生成的文件的后缀

        eventProducer.fireEvent(event);
        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
        map.put("shareUrl", shareUrl + "/" + fileName);
        return ForumUtil.getJSONString("0", null, map);
    }


    /**
     * 废弃该方法 因为图片已从七牛云上获取
     * @param fileName
     * @param response
     */
    @RequestMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("非法的文件名称!");
        }
        // 设置响应的格式
        response.setContentType("image/png");
        File file = new File(saveImagePath + "\\" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败!" + e.getMessage());
        }
    }

}
