package com.cn.com.cqucc.forum.event;

import com.alibaba.fastjson.JSONObject;
import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.dao.elasticsearch.DiscussPostRepository;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.entity.Event;
import com.cn.com.cqucc.forum.entity.Message;
import com.cn.com.cqucc.forum.service.ElasticSearchService;
import com.cn.com.cqucc.forum.service.MessageService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.ForumUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Value("${wk.image.command}")
    private String wkCommand;

    @Value("${wk.image.storage}")
    private String saveImagePath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler; // 可以定时执行一个任务

    /**
     * 消费者处理消息
     *
     * @param record 用于接收 生产者的消息
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handlerMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        Message message = new Message();

        message.setFromId(SYSTEM_ID);// 将发送者id设置为一个常量 方便阅读
        message.setToId(event.getEntityUserId());
        message.setStatus(0);
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        // 内容包含的是一个对象
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId()); //触发事件的用户id
        content.put("entityType", event.getEntityType()); // 被触发事件的类型
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) { // 将生产者中得到的 数据 封装到 message的内容中去
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlerDiscusPostPublish(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("搜索结果为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("搜索内容格式错误!");
            return;
        }

        // 直接保存到ES中
        DiscussPost post = discussPostMapper.selectDiscussPost(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handlerDiscusPostDelete(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("搜索结果为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("搜索内容格式错误!");
            return;
        }

        // 直接从ES中将该帖子删除
        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }


    @KafkaListener(topics = TOPIC_SHARE)
    public void handlerImageShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("分享失败!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("分享格式错误!");
            return;
        }

        // 获取到event中的数据 执行命令
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        // 拼接命令
        String command = wkCommand + " --quality 75 " + htmlUrl + " " + saveImagePath + "\\" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(command);
            logger.info("分享图片成功!" + command);
        } catch (IOException e) {
            logger.error("分享图片失败!" + e.getMessage());
        }

        //启用定时器主要是监视该图片一旦生成，则上传到七牛云。
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500); // 每半秒执行一次
        task.setFuture(future); // 将该参数设置回去 用于后面停止定时器使用
    }

    class UploadTask implements Runnable {
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;

        // 任务开始执行时间
        private long startTime;

        // 任务执行次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 时间超过30秒一般是生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) { // 如果我们任务执行时间超出30秒终止任务
                logger.error("执行时间过长终止任务!" + fileName);
                future.cancel(true);
                return;
            }

            //上传失败!
            if (uploadTimes > 3) {
                logger.error("上传次数过多，终止任务!" + fileName);
                future.cancel(true);
                return;
            }
            // 开始执行上传任务
            String path = saveImagePath + "\\" + fileName + suffix;
            System.out.println(path);
            File file = new File(path);
            if (file.exists()) {
                // 如果需要上传的文件路径存在
                logger.info(String.format("开始第[%d]次上传[%s]", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", ForumUtil.getJSONString("0"));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 执行上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));

                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix.replace('.', ' '), false
                    );
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第[%d]次上传失败[%s]", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第[%d] 次 上传图片成功[%s]", uploadTimes, fileName));
                        future.cancel(true); // 结束任务
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第[%d]次上传失败[%s]", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成!..................." + fileName);
            }
        }
    }
}
