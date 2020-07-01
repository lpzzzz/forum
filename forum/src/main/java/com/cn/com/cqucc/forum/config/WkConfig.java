package com.cn.com.cqucc.forum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 声明一个配置类 ： 当启动服务的时候会自动读取配置类 创建一个对象 这时便创建一个存放生成的图片文件
 */
@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String dirPath;

    @PostConstruct
    public void init() {
        // 创建文件夹
        File file = new File(dirPath);
        if (!file.exists()) { // 如果该文件夹不存在
            // 创建文件夹
            file.mkdirs();
            logger.info("创建 wk 图片目录 ： " + dirPath);
        }
    }

}
