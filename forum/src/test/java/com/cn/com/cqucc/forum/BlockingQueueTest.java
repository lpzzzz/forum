package com.cn.com.cqucc.forum;


import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10); // 初始化为10个大小的空间
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

/**
 * 生产者
 */
class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    /**
     * 构造方法初始化 阻塞队列
     *
     * @param queue
     */
    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20); // 每20秒生产一个
                // 生产者生产100次
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + " 生产 ： " + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者
 */
class Consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + " 消费 ： " + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
