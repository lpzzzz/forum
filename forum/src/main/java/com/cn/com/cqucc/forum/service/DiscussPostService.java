package com.cn.com.cqucc.forum.service;

import com.cn.com.cqucc.forum.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiscussPostService {

    public List<DiscussPost> selectDiscussPosts(String userId, int offset, Integer limit,int orderMode);

    int selectDiscussPostRows(String userId);

    int insertDiscussPost(DiscussPost post);

    DiscussPost selectDiscussPost(Integer id);

    int updateDiscussPostCommentCount(int id,int count);

    int updateDiscussPostType(int id , int type);

    int updateDiscussPostStatus(int id , int status);

    int updateDiscussPostScore(Integer postId, double score);
}
