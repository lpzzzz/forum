package com.cn.com.cqucc.forum.config;

import com.cn.com.cqucc.forum.quartz.AlphaJob;
import com.cn.com.cqucc.forum.quartz.PostScoreRefreshJob;
import com.cn.com.cqucc.forum.service.impl.AlphaService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartConfig {

    //FactoryBean可以简化Bean的实例化过程
    // 1. 通过FactoryBean封装Bean的实例化过程
    // 2. 将FactoryBean装配到Spring容器中
    // 3. 将FactoryBean注入给其他Bean
    // 4. 该Bean得到的是FactoryBean所管理的对象实例

    /**
     * 配置JobDetail
     *
     * @return
     */
    //@Bean
    /*public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true); // 是否持久保存
        factoryBean.setRequestsRecovery(true);// 是否是可以恢复的
        return factoryBean;
    }*/


    /**
     * 配置Trigger (SimpleTriggerFactoryBean，CronTriggerFactoryBean)
     *
     * @param alphaJobDetail
     * @return
     */
    //@Bean
   /* public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) { //这里的参数名称与上面的方法名称保持一致方便容器进行注入
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000); // 每3秒执行一次
        factoryBean.setJobDataAsMap(new JobDataMap());
        return factoryBean;
    }*/


    /**
     * 配置刷新帖子分数 任务
     *
     * @return
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("forumJobGroup");
        factoryBean.setDurability(true); // 是否持久保存
        factoryBean.setRequestsRecovery(true);// 是否是可以恢复的
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) { //这里的参数名称与上面的方法名称保持一致方便容器进行注入
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("forumTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5); //设置事件间隔 每5分钟执行一次
        factoryBean.setJobDataAsMap(new JobDataMap());
        return factoryBean;
    }
}
