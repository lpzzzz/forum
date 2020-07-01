package com.cn.com.cqucc.forum.dao.impl;

import com.cn.com.cqucc.forum.dao.AlphaDao;
import org.springframework.stereotype.Repository;


@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
