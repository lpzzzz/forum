package com.cn.com.cqucc.forum.dao;


import com.cn.com.cqucc.forum.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper // 这里需要使用@Mapper注解进行设置才能有效果 @Repository注解无效
public interface DiscussPostMapper {

    /**
     * 首页帖子数据显示
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode 根据传递的排序方式参数 进行判断按照什么方式进行排序 0 按帖子最新发布时间排序 1 表示按照分数进行排序
     * @return
     */
    // 分页 ：一共有多少掉数据 每页显示多少条数据
    List<DiscussPost> selectDiscussPosts(@Param("userId") String userId,
                                         @Param("offset") int offset,
                                         @Param("limit") Integer limit,
                                         @Param("orderMode") int orderMode);


    //@Param() 注解是用于为参数起别名，
    // 如果只有一个参数，并且在<if>里使用，则必须加别名。
    int selectDiscussPostRows(@Param("userId") String userId);

    int insertDiscussPost(DiscussPost post);

    /**
     * 根据id查询帖子
     *
     * @param id
     * @return
     */
    DiscussPost selectDiscussPost(Integer id);

    /**
     * 根据帖子id修改帖子评论数量
     *
     * @param id    帖子id
     * @param count 评论数量
     * @return
     */
    int updateDiscussCommentCount(@Param("id") int id, @Param("count") int count);

    /**
     * 修改帖子类型 0-普通帖子 1-置顶
     *
     * @param id
     * @param type
     * @return
     */
    int updateDiscussPostType(@Param("id") int id, @Param("type") int type);

    /**
     * 修改帖子状态 0-正常 1-加精 2-删除
     *
     * @param id
     * @param status
     * @return
     */
    int updateDiscussPostStatus(@Param("id") int id, @Param("status") int status);

    int updateDiscussPostScore(@Param("postId") Integer postId, @Param("score") double score);
}
