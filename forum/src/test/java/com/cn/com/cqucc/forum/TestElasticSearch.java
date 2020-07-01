package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.dao.DiscussPostMapper;
import com.cn.com.cqucc.forum.dao.elasticsearch.DiscussPostRepository;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class TestElasticSearch {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPost(280));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(274));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(275));
    }

    /**
     * 添加某个id下的全部数据
     */
    @Test
    public void testInsertAll() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(154 + "", 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(156 + "", 0, 100, 0));
    }

    @Test
    public void update() {
        DiscussPost post = discussPostMapper.selectDiscussPost(216);
        post.setTitle("互联网寒冬......");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(231);
    }

    @Test
    public void testDeleteAll() {
        discussPostRepository.deleteAll(); // 删除全部的数据
    }

    /**
     * 搜索
     */
    @Test
    public void testSearchByRepository() {
        // 构造搜索条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                /*指定哪些字段高亮显示*/
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em")
                ).build();
        // 底层获取到了高亮显示的值但是没有做进一步的处理
        //
        Page<DiscussPost> search = discussPostRepository.search(searchQuery);
        System.out.println(search.getTotalPages());
        System.out.println(search.getTotalElements());
        System.out.println(search.getNumber());
        System.out.println(search.getSize());
        for (DiscussPost post : search) {
            System.out.println(post);
        }
    }


    /**
     * 如何处理高亮的数据 将需要高亮的数据加上标签
     */
    @Test
    public void testSearchByTemplate() {
        // 构造搜索条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                /*指定哪些字段高亮显示*/
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em")
                ).build();
        // 底层获取到了高亮显示的值但是没有做进一步的处理

        Page<DiscussPost> search = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {

            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                SearchHits hits = searchResponse.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> postList = new ArrayList<>();

                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));
                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(userId);
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);
                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);
                    String type = hit.getSourceAsMap().get("type").toString();
                    post.setType(Integer.valueOf(type));
                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));
                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    // 在ElasticSearch中存储的时间是一个Long类型的数据
                    post.setCreateTime(new Date(Long.valueOf(createTime)));
                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的内容
                    HighlightField highlightTitle = hit.getHighlightFields().get("title");
                    if (highlightTitle != null) {
                        post.setTitle(highlightTitle.getFragments()[0].toString());
                    }

                    HighlightField highlightContent = hit.getHighlightFields().get("content");
                    if (highlightContent != null) {
                        post.setContent(highlightContent.getFragments()[0].toString());
                    }
                    postList.add(post);
                }

                return new AggregatedPageImpl(postList, pageable, hits.getTotalHits(),
                        searchResponse.getAggregations(),
                        searchResponse.getScrollId(),
                        hits.getMaxScore());
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }
        });
        System.out.println(search.getTotalPages());
        System.out.println(search.getTotalElements());
        System.out.println(search.getNumber());
        System.out.println(search.getSize());
        for (DiscussPost post : search) {
            System.out.println(post);
        }
    }

}
