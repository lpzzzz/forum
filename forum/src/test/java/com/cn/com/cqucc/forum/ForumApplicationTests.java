package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.AlphaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootTest
class ForumApplicationTests implements ApplicationContextAware{
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testAlphaDao() {
        System.out.println(applicationContext);
        AlphaDao ad = applicationContext.getBean(AlphaDao.class);
        System.out.println(ad.select());
        AlphaDao ah = applicationContext.getBean("alphaHibernate", AlphaDao.class);
        System.out.println(ah.select());
    }

}
