package com.cn.com.cqucc.forum.dao.impl;

import com.cn.com.cqucc.forum.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;


@Repository
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
