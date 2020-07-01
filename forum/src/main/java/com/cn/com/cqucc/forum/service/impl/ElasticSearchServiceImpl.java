package com.cn.com.cqucc.forum.service.impl;

import com.cn.com.cqucc.forum.dao.elasticsearch.DiscussPostRepository;
import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.service.ElasticSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    @Override
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    @Override
    public Page<DiscussPost> searchDiscussPost(String keywords, int current, int limit) {

        // 构造搜索条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keywords, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                /*指定哪些字段高亮显示*/
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 底层获取到了高亮显示的值但是没有做进一步的处理
        // 需要使用ElasticSearchTemplate中的方法进行进一步的处理
        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {

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
                    String score = hit.getSourceAsMap().get("score").toString();
                    post.setScore(Double.valueOf(score));

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
    }
}
