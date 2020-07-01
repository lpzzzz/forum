package com.cn.com.cqucc.forum.dao;

import com.cn.com.cqucc.forum.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    /**
     * 插入一个登录凭证
     *
     * @param loginTicket
     * @return
     */
    @Insert({
            /*注意在每一行的sql后面加上一个空格以免拼接的时候出错*/
            "insert into login_ticket (user_id, ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    /*设置主键自动增长*/
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    /**
     * 查询一个登录凭证
     *
     * @param ticket
     * @return
     */
    @Select({
            "select id,user_id, ticket, status, expired ",
            "from login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectLoginTicket(String ticket);


    @Update({
            "<script> ",
            "update login_ticket set status = #{status} ",
            "where ticket = #{ticket} ",
            "<if test=\"ticket != null\"> ",
            "and 1 = 1 ",
            "</if> ",
            "</script>"
    })
        //@Param() 注解是用于为参数起别名，
        // 如果只有一个参数，并且在<if>里使用，则必须加别名。
    int updateLoginTicket(@Param("ticket") String ticket, @Param("status") int status);
}
