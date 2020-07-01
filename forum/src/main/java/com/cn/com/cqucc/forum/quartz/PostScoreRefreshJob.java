package com.cn.com.cqucc.forum.quartz;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import com.cn.com.cqucc.forum.service.ElasticSearchService;
import com.cn.com.cqucc.forum.service.LikeService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import com.cn.com.cqucc.forum.util.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private DiscussPostService discussPostService;

    // 网站成立时间 需要在静态代码块中进行初始化 因为只使用一次
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化网站纪元失败!");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        // 如果在没有人做任何操作的时候
        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数!" + operations.size());

        while (operations.size() > 0) { // 只要有数据就可以进行计算
            this.refresh((Integer) operations.pop());// operations.pop()弹出一个帖子Id
        }

        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.selectDiscussPost(postId);

        if (post == null) {
            logger.error("该帖子不存在!" + postId);
            return;
        }

        // 计算分数
        // 1. 判断是否 加精
        boolean wonderful = post.getStatus() == 1;
        // 2. 获得评论的数量
        Integer commentCount = post.getCommentCount();
        // 3. 获得点赞的数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 4. 计算权重 如果帖子为加精的帖子我们自动加分 75 否则 为 0
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        // 分数 = log10(权重) + 距离天数 log10(w) 这里的 w 可能会小于 1 或者小于 0 小于0可能出现 负数 Math.max(w,1) 选择两数中的大数进行计算
        double score = Math.log10(Math.max(w, 1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子的分数
        discussPostService.updateDiscussPostScore(postId, score);
        // 更新ES中数据
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
