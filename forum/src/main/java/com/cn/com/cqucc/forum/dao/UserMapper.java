package com.cn.com.cqucc.forum.dao;

import com.cn.com.cqucc.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
//    @Select("select * from user where id = #{id}")
    public User selectById(Integer id);

    /**
     * 根据用户名称查询用户信息
     *
     * @param userName
     * @return
     */
    public User selectByUserName(String userName);

    /***
     * 根据邮箱查询用户信息
     * @param email
     * @return
     */
    public User selectByEmail(String email);

    /**
     * 插入用户信息
     * @param user
     * @return
     */
    public int insertUser(User user);

    /**
     * 根据id 修改用户的当前状态
     * @param id
     * @param status
     * @return
     * 注意事项：而当传递多个参数的时候, 则需要再Mapper代码的参数前面加上@Param("xx")来做定义.
     */
    public int updateUserStatus(@Param("id") Integer id , @Param("status") Integer status);

    /**
     * 根据id 修改用户头像
     * @param id
     * @param headerUrl
     * @return
     */
    public int  updateUserHeader(@Param("id") Integer id , @Param("headerUrl") String headerUrl);

    /**
     * 根据 id 修改用户密码
     * @param id
     * @param password
     * @return
     */
    public int updateUserPassword(@Param("id") Integer id, @Param("password") String password);

    User selectUserByUserName(String userName);
}
