package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * 单元测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;
    DiscussPost post;

    @BeforeTestClass // 整个项目运行过程中只执行一次 所以方法需要加上static修饰
    public static void beforeClassTest() {
        System.out.println("beforeClass");
    }

    @AfterTestClass
    public static void afterClassTest() {
        System.out.println("afterClass");
    }


    @Before  // 每个方法执行一次该注解标识的方法执行一次
    public void beforeTest() { // 在@Before注解标识的方法中初始化数据
        post = new DiscussPost();
        post.setTitle("测试数据");
        post.setContent("测试数据");
        post.setCreateTime(new Date());
        post.setType(0);
        post.setCommentCount(0);
        post.setUserId(154 + "");
        post.setStatus(0);
        post.setScore(0.0);
        discussPostService.insertDiscussPost(post);
        System.out.println("before");
    }


    @After
    public void afterTest() {
        // 在一个方法执行完成之后 删除数据
        discussPostService.updateDiscussPostStatus(Integer.valueOf(post.getUserId()), 2);
        System.out.println("after");
    }


    @Test
    public void test1() {
        System.out.println("test1");
    }


    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testSelectById() {
        DiscussPost testPost = discussPostService.selectDiscussPost(Integer.valueOf(post.getId()));
        System.out.println();
        // 使用断言判断方法是否按照预期执行
        Assert.assertNotNull(post);// 对象不为空
        Assert.assertEquals(post.getTitle(), testPost.getTitle());
        Assert.assertEquals(post.getContent(), testPost.getContent());
        Assert.assertEquals(post.getScore(), testPost.getScore());
    }


    @Test
    public void updateScore() {
        int rows = discussPostService.updateDiscussPostScore(post.getId(), 2000.0);
        // 断言更新的行数是1
        Assert.assertEquals(rows,1);
        DiscussPost testPost = discussPostService.selectDiscussPost(Integer.valueOf(post.getId()));
        Assert.assertEquals(2000.00,testPost.getScore(),2); // 断言小数的时候需要加上精确到几位小数
    }

}
