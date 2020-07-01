package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.dao.UserMapper;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.util.ForumUtil;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

@Service
public class AlphaService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 编程式事务控制
    @Autowired
    private TransactionTemplate transactionTemplate;


    /**
     * 使用注解的方式实现事务的控制
     *
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object saveUser() {
        User user = new User();
        user.setUserName("张三");
        user.setCreateTime(new Date());
        user.setSalt(ForumUtil.generateUUID().substring(0, 5));
        user.setPassword(ForumUtil.md5("123" + user.getSalt()));
        userMapper.insertUser(user);
        DiscussPost post = new DiscussPost();
        post.setTitle("新人报道!");
        post.setContent("新报道，多多关照");
        post.setUserId(user.getId() + "");
        post.setCreateTime(new Date());

        discussPostMapper.insertDiscussPost(post);
        // 制造一个异常
//        int  i = 3/0;
        return "ok";
    }

    /**
     * 编程式事务控制
     *
     * @return
     */
    public Object save() {
        // 设置隔离级别
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        // 设置传播方式
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // 需要实现一个方法
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                User user = new User();
                user.setUserName("李四");
                user.setCreateTime(new Date());
                user.setSalt(ForumUtil.generateUUID().substring(0, 5));
                user.setPassword(ForumUtil.md5("123" + user.getSalt()));
                userMapper.insertUser(user);
                DiscussPost post = new DiscussPost();
                post.setTitle("新人报道!");
                post.setContent("新报道，多多关照");
                post.setUserId(user.getId() + "");
                post.setCreateTime(new Date());

                discussPostMapper.insertDiscussPost(post);
                // 制造一个异常
//                int i = 3 / 0;
                return "ok";
            }
        });
    }

    /**
     * Spring 普通线程池简化演示
     */
    @Async
    public void testThreadPoolTaskExecutor() {
        logger.debug("hello testThreadPoolTaskExecutor");
    }

    /**
     * 注解中第一个参数表示 延迟多少秒执行 第二个参数表示每隔多少秒执行一次
     */
    /*@Scheduled(initialDelay = 10000, fixedRate = 1000)*/
    public void testThreadPoolTaskScheduler() {
        logger.debug("hello ThreadPoolTaskScheduler");
    }
}
