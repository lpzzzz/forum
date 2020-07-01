package com.cn.com.cqucc.forum;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        // 让主线程有sleep 几秒
        kafkaProducer.sendMessage("test", "你好!");
        kafkaProducer.sendMessage("test", "我来啦!");
        // 生产者发消息 使我们主动调的 消费者处理消息是主动调的
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class KafkaConsumer {

    @KafkaListener(topics = "test") // 监听的主题是 test
    public void handlerMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}