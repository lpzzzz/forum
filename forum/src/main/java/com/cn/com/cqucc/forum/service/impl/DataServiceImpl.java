package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.service.DataService;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void recordUv(String ip) {
        String redisKey = RedisUtil.getUvKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    @Override
    public long calculatorUv(Date startDate, Date endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 如何取得区间内的key?
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) { // 如果在此区间内继续执行
            String uvKey = RedisUtil.getUvKey(df.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }

        // 合并数据
        String redisKey = RedisUtil.getUvKey(df.format(startDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    @Override
    public void recordDau(int userId) {
        String dauKey = RedisUtil.getDauKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true); // id位置处的值设置为true 表示访问一次 为活跃用户
    }

    @Override
    public long calculatorDau(Date startDate, Date endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String dauKey = RedisUtil.getDauKey(df.format(calendar.getTime())); // 获取当前时间
            keyList.add(dauKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisUtil.getDauKey(df.format(startDate), df.format(endDate));
                connection.bitOp(RedisStringCommands.BitOperation.OR, dauKey.getBytes(),
                        keyList.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        });
    }
}
