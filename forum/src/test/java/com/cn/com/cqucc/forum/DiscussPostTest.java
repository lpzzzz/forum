package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DiscussPostTest {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts("101", 1, 5,0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void testSelectDiscussPostRows() {
        System.out.println(discussPostService.selectDiscussPostRows("0"));
    }

    @Test
    public void testInsertDiscussPost() {
        DiscussPost post = new DiscussPost();
        post.setTitle("你好!");
        post.setContent("456899865552222");
        int i = discussPostMapper.insertDiscussPost(post);
        System.out.println(i);
    }

    @Test
    public void testSelectDiscussPost() {
        System.out.println(discussPostMapper.selectDiscussPost(288));
    }
}
