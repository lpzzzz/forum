package com.cn.com.cqucc.forum.util;

/**
 * 生成 redis 的key 工具类
 */
public class RedisUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";

    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_USER_CACHE = "userCache";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

    // 格式 like:entity:entityType:entityId -> set(userId) 根据userId统计点赞数量
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 返回格式 like:user:userId
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体 被关注对象的key
     *
     * @param userId     关注者的id
     * @param entityType 被关注对象类型 利于扩展
     * @return
     */

    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }


    /**
     * 某个实体拥有的粉丝
     *
     * @param entityId   实体的id
     * @param entityType 被关注者的id
     * @return
     */
    // 格式 follower : entityType : entityId -> zset(userId,now)
    public static String getFollowerKey(int entityId, int entityType) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 将验证码存入到redis中 的 key 构造
     *
     * @param owner 是服务端发送到浏览器的一个cookie标识
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证key
     *
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }


    /**
     * redis缓存用户信息key
     *
     * @param userId
     * @return
     */
    public static String getUserCacheKey(int userId) {
        return PREFIX_USER_CACHE + SPLIT + userId;
    }

    /**
     * 单日uv key
     *
     * @param date
     * @return
     */
    public static String getUvKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间uv key
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getUvKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日DAU key
     *
     * @param date
     * @return
     */
    public static String getDauKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间Dau key
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDauKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }


    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}
