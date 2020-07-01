package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class CaffeineTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostService discussPostService;

    // 添加帖子数据 以作为压力测试
    @Test
    public void initPostData() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setTitle("测试数据");
            post.setContent("测试数据");
            post.setCreateTime(new Date());
            post.setType(0);
            post.setCommentCount(0);
            post.setUserId(154 + "");
            post.setStatus(0);
            post.setScore(0.0);
            discussPostMapper.insertDiscussPost(post);
        }
    }

    @Test
    public void testLoadingCache() {
        System.out.println(discussPostService.selectDiscussPosts("0", 0, 10, 1));
        System.out.println(discussPostService.selectDiscussPosts("0", 0, 10, 1));
        System.out.println(discussPostService.selectDiscussPosts("0", 0, 10, 1));
        System.out.println(discussPostService.selectDiscussPosts("0", 0, 10, 0));
    }
}
