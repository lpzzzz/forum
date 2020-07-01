package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.service.impl.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ThreadPoolTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK可执行定时任务线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring 普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring 可以执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    /**
     * 对sleep方法进行封装
     * @param m
     */
    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // JDK普通线程池
    @Test
    public void testExecutorService() {
        // 实现Runable接口
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ExecutorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        sleep(10000);
    }

    // JDK定时任务线程池
    @Test
    public void testScheduleExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ScheduleExecutorService");
            }
        };

        // 延迟10秒执行 每个线程执行间隔为1秒
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MICROSECONDS);// 间隔多少秒执行一个线程
        sleep(30000);
    }


    // Spring 线程池需要配置 配置文件

    // Spring 普通线程池
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }


    /**
     * Spring可以执行定时任务的线程池
     */
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskScheduler");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task, startTime, 1000);// 默认就是毫秒值
        sleep(30000);
    }

    /**
     * Spring普通线程池简化
     */
    @Test
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            alphaService.testThreadPoolTaskExecutor();
        }
        sleep(30000);
    }

    /**
     * Spring 可以执行定时任务的线程池简化
     */
    @Test
    public void testThreadPoolTaskSchedulerSimple() {
        sleep(30000);
    }
}
