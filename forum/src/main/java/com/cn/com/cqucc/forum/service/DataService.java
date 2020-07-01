package com.cn.com.cqucc.forum.service;

import java.util.Date;

/**
 * 数据统计业务层接口
 */
public interface DataService {

    /**
     * 单日访客
     * @param ip
     */
    public void recordUv(String ip);

    /**
     * 某区间内的访客
     * @param startDate
     * @param endDate
     * @return
     */
    public long calculatorUv(Date startDate,Date endDate);

    /**
     * 单日活跃用户
     * @param userId
     */
    public void recordDau(int userId);

    /**
     * 某区间内的活跃用户
     * @param startDate
     * @param endDate
     * @return
     */
    public long calculatorDau(Date startDate , Date endDate);
}
