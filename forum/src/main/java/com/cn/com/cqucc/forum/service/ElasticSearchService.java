package com.cn.com.cqucc.forum.service;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ElasticSearchService {

    /**
     * 保存一份帖子数据到ElasticSearch中去
     * 该方法也可以作为修改的方法
     *
     * @param post
     */
    public void saveDiscussPost(DiscussPost post);

    /**
     * 从ElasticSearch中根据id删除一份数据
     *
     * @param id
     */
    public void deleteDiscussPost(int id);


    /**
     * 根据关键词进行搜索
     *
     * @param keywords
     * @param current
     * @param limit
     * @return
     */
    public Page<DiscussPost> searchDiscussPost(String keywords, int current, int limit);
}
