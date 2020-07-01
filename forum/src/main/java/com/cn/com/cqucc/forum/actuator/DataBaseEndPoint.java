package com.cn.com.cqucc.forum.actuator;


import com.cn.com.cqucc.forum.util.ForumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
public class DataBaseEndPoint {

    private static final Logger logger =
            LoggerFactory.getLogger(DataBaseEndPoint.class);


    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    @ReadOperation // 表示只能是get请求
    public String checkConnection() {

        try (Connection conn = dataSource.getConnection()) {
            return ForumUtil.getJSONString("0", "获取连接成功!");
        } catch (SQLException e) {
            logger.error("获取连接失败" + e.getMessage());
            return ForumUtil.getJSONString("1", "获取连接失败!");
        }
    }
}
