package com.cn.com.cqucc.forum.dao.elasticsearch;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 1.ElasticsearchRepository 继承这个 接口 指定泛型 第一个为 处理的类型 第二个为 类型的主键类型
 * 2. 还得将其交给容器进行管理
 */

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>{
}
