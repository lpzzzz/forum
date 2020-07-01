package com.cn.com.cqucc.forum;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QuartzTest {

    @Autowired // 注入调度器
    private Scheduler scheduler;

    /**
     * 使用调度器删除指定的job
     */
    @Test
    public void testDeleteJob() {
        try {
           boolean result =  scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
