package com.cn.com.cqucc.forum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 数据类型 string
     */
    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);
        //获取值
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        // 值加一
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        // 值减一
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    /**
     * 数据类型hash
     */
    @Test
    public void testHash() {
        String redisKey = "test:user";

        // 设置值
        redisTemplate.opsForHash().put(redisKey, "id", "1");
        redisTemplate.opsForHash().put(redisKey, "username", "张三");
        // 获取值
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }


    /**
     * 数据类型list
     */
    @Test
    public void testList() {
        String redisKey = "test:ids";

        //设置值
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);
        redisTemplate.opsForList().leftPush(redisKey, 104);

        // 获取值
        System.out.println("获取列表中的元素个数" + redisTemplate.opsForList().size(redisKey));
        System.out.println("获取列表中第0个位置处的元素的值" + redisTemplate.opsForList().index(redisKey, 0));
        // 获取某一个范围内的元素值
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 3));
        // 弹出列表中的元素
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    /**
     * 数据类型set
     */
    @Test
    public void testSet() {

        String redisKey = "test:teachers";

        // 设置值
        redisTemplate.opsForSet().add(redisKey, "aaa", "bbb", "ccc", "ddd");
        // 获取值
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        // 判断某元素是否是集合中的元素
        System.out.println(redisTemplate.opsForSet().isMember(redisKey, "bbb"));
        // 获取集合中的数据个数
        System.out.println("集合中有多少个元素" + redisTemplate.opsForSet().size(redisKey));
        // 从集合中随机弹出一个元素
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
    }

    /**
     * 数据类型有序集合 ZSet
     */
    @Test
    public void testZSet() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "张三", 10);
        redisTemplate.opsForZSet().add(redisKey, "李四", 20);
        redisTemplate.opsForZSet().add(redisKey, "王五", 30);
        redisTemplate.opsForZSet().add(redisKey, "赵六", 40);
        redisTemplate.opsForZSet().add(redisKey, "田七", 50);

        // 统计有序集合中的元素个数
        System.out.println("有序集合中元素的个数 ： " + redisTemplate.opsForZSet().zCard(redisKey));
        // 统计 某人的分数
        System.out.println("张三 的 分数是 ：" + redisTemplate.opsForZSet().score(redisKey, "张三"));
        // 统计某人的排名
        System.out.println("张三 的 排名是 ：" + redisTemplate.opsForZSet().rank(redisKey, "张三"));
        // 统计某一个范围类的元素
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
    }

    /**
     * redis全局的命令
     */
    @Test
    public void testKeys() {
        // 删除一个对象
        redisTemplate.delete("test:user");
        // 查询一个对象是否存在
        System.out.println(redisTemplate.hasKey("test:user"));
        // 设置某一个对象的过期时间 设置单位为秒
        redisTemplate.expire("test:count", 10, TimeUnit.SECONDS);
    }

    // 多次访问同一个key 已绑定的形式进行简化
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        // 将redisKey 进行绑定
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.set(1);
        // 进行累加
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get()); // 6
    }

    // redis 的事务 ： 不是完全满足关系型数据库事务管理 的 四个特点
    // redis 启动事务之后 在我们输入一个命令的时候 redis服务器并不会立即执行该命令而是将命令存到队列中，
    // 知道操作完之后提交事务的时候才会将会所有命令发送给redis服务器，这里将出现一个隐含的问题：如果在事务中间执行一个查询将不会返回一个结果。
    // 要么提前查、要么就在提交事务之后在查询
    // 一般通常使用编程式事务将事务的范围缩小。

    /**
     * 编程式事务：redis
     */
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                redisOperations.multi(); // 开启事务
                redisTemplate.opsForSet().add(redisKey, "aaa", "bbb", "ccc", "ddd");
                // 执行查询方法测试
                System.out.println(redisTemplate.opsForSet().members(redisKey)); //[] 这里返回的是一个空集合 由于还未执行插入操作
                return redisOperations.exec(); // 执行并返回
            }
        });

        // 查询redis 集合中的数据
        System.out.println(redisTemplate.opsForSet().members("test:tx"));
        System.out.println(obj);
    }


    // 统计 20 万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey01 = "test:hll:01";

        for (int i = 1; i <= 100000; i++) {
            // 制造10万条数据存入到 redis中
            redisTemplate.opsForHyperLogLog().add(redisKey01, i);
        }

        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);//制造 1 - 100000 的随机整数
            redisTemplate.opsForHyperLogLog().add(redisKey01, r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey01));
    }


    /**
     * 将三组数据合并，再统计合并后的重复数据的独立总数
     */
    @Test
    public void testHyperLogLogUnion() {

        String redisKey02 = "test:hll:02";

        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey02, i);
        }

        String redisKey03 = "test:hll:03";

        for (int i = 1; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey03, i);
        }

        String redisKey04 = "test:hll:04";
        for (int i = 15001; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey04, i);
        }

        String unionKey = "test:hll:union";
        // 合并去重
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey02, redisKey03, redisKey04);

        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    /**
     * 统计Bitmap中为true的个数
     */
    @Test
    public void testBitmap() {
        String redisKey01 = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey01, 1, true);
        redisTemplate.opsForValue().setBit(redisKey01, 3, true);
        redisTemplate.opsForValue().setBit(redisKey01, 5, true);
        redisTemplate.opsForValue().setBit(redisKey01, 6, true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey01, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey01, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey01, 3));

        // 统计数据需要获取 connection
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey01.getBytes());
            }
        });
        // 统计的是为true的个数
        System.out.println(obj);
    }

    /**
     * Bitmap中的OR运算
     */
    @Test
    public void testBitmapOperation() {
        String redisKey02 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey02, 0, true);
        redisTemplate.opsForValue().setBit(redisKey02, 1, true);
        redisTemplate.opsForValue().setBit(redisKey02, 2, true);

        String redisKey03 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey03, 2, true);
        redisTemplate.opsForValue().setBit(redisKey03, 3, true);
        redisTemplate.opsForValue().setBit(redisKey03, 4, true);

        String redisKey04 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey04, 4, true);
        redisTemplate.opsForValue().setBit(redisKey04, 5, true);
        redisTemplate.opsForValue().setBit(redisKey04, 6, true);

        String redisKeyOr = "test:bm:or";

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKeyOr.getBytes(),
                        redisKey02.getBytes(), redisKey03.getBytes(), redisKey04.getBytes());
                return connection.bitCount(redisKeyOr.getBytes());
            }
        });

        for (int i = 0; i < 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr, i));
        }

        System.out.println(obj);
    }

}
