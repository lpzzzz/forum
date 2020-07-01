package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.CommentMapper;
import com.cn.com.cqucc.forum.entity.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CommentTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void testSelectComment() {
        List<Comment> comments = commentMapper.selectCommentByEntity(1, 228, 0, 10);

        for (Comment comment : comments) {
            System.out.println(comment);
        }

        System.out.println("评论数量: " + commentMapper.selectCommentCountByEntity(1, 228));

    }
}
