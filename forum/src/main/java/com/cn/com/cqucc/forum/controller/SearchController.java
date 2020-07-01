package com.cn.com.cqucc.forum.controller;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import com.cn.com.cqucc.forum.entity.Page;
import com.cn.com.cqucc.forum.service.ElasticSearchService;
import com.cn.com.cqucc.forum.service.LikeService;
import com.cn.com.cqucc.forum.service.UserService;
import com.cn.com.cqucc.forum.util.ForumConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索功能表现层实现
 */
@Controller
public class SearchController implements ForumConstant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // 访问路径格式 path = "/search?keywords = "
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keywords, Page page, Model model) {
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticSearchService.searchDiscussPost(keywords, page.getCurrentPage() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> postList = new ArrayList<>();

        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                //作者
                map.put("user", userService.selectById(Integer.valueOf(post.getUserId())));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                System.out.println(likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                postList.add(map);
            }
        }

        model.addAttribute("postList", postList);
        model.addAttribute("keywords", keywords); //将输入的关键词返回到页面 拼接路径使用

        // 分页信息
        page.setPath("/search?keywords=" + keywords);
        page.setRows(searchResult != null ? (int) searchResult.getTotalElements() : 0);

        return "site/search"; // 返回到页面
    }

}
