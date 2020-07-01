package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.controller.DiscussPostController;
import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.DiscussPostService;
import com.cn.com.cqucc.forum.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Value("${caffeine.posts.max.size}")
    private int macSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine 核心接口：Cache、LoadingCache、AsyncLoadingCache
    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListeCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // hua缓存只需在服务器启动的时候创建一次
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListeCache = Caffeine.newBuilder()
                .maximumSize(macSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {

                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数有误!");
                        }

                        String[] params = key.split(":");

                        if (params == null || params.length == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("初始化缓存来自DB");
                        return discussPostMapper.selectDiscussPosts("0", offset, limit, 1);
                    }
                });

        postRowsCache = Caffeine.newBuilder()
                .maximumSize(macSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("查询帖子总数来自DB");
                        return discussPostMapper.selectDiscussPostRows(key + "");
                    }
                });
    }

    @Override
    public List<DiscussPost> selectDiscussPosts(String userId, int offset, Integer limit, int orderMode) {
        if (Integer.valueOf(userId) == 0 && orderMode == 1) {
            return postListeCache.get(offset + ":" + limit);
        }

        logger.debug("load post list from DB.");

        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    @Override
    public int selectDiscussPostRows(String userId) {
        if (Integer.valueOf(userId) == 0) {
            return postRowsCache.get(Integer.valueOf(userId));
        }
        logger.debug("load rows list from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int insertDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("帖子参数不能为空!");
        }
        // 转义内容 比如遇到 标题和内容出现标签的时候需要进行转义
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 敏感词过滤
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    @Override
    public DiscussPost selectDiscussPost(Integer id) {
        return discussPostMapper.selectDiscussPost(id);
    }

    @Override
    public int updateDiscussPostCommentCount(int id, int count) {
        return discussPostMapper.updateDiscussCommentCount(id, count);
    }

    @Override
    public int updateDiscussPostType(int id, int type) {
        return discussPostMapper.updateDiscussPostType(id, type);
    }

    @Override
    public int updateDiscussPostStatus(int id, int status) {
        return discussPostMapper.updateDiscussPostStatus(id, status);
    }

    @Override
    public int updateDiscussPostScore(Integer postId, double score) {
        return discussPostMapper.updateDiscussPostScore(postId, score);
    }
}
