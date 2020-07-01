package com.cn.com.cqucc.forum.service;

import com.cn.com.cqucc.forum.entity.LoginTicket;
import com.cn.com.cqucc.forum.entity.User;
import org.elasticsearch.common.recycler.Recycler;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    public User selectById(Integer id);


    /**
     * 根据用户名查询用户信息
     *
     * @param userName
     * @return
     */
    public User selectByUserName(String userName);


    /**
     * 根据邮箱查询用户信息
     *
     * @param eamil
     * @return
     */
    public User selectByEmail(String eamil);


    /**
     * 插入用户信息
     *
     * @param user
     * @return
     */
    public int insertUser(User user);


    /**
     * 根据id修改用户的状态
     *
     * @param id
     * @param status
     * @return
     */
    public int updateUserStatus(int id, int status);


    /**
     * 根据id修改用户的头像
     */
    public int updateUserHeader(int id, String headerUrl);


    /**
     * 根据用户id修改用户的密码
     *
     * @param id
     * @param password
     * @return
     */
    public int updateUserPassword(int id, String password);


    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    public Map<String, Object> register(User user);


    /**
     * 激活用户
     *
     * @param userId
     * @param code
     * @return
     */
    public int activation(Integer userId, String code);


    /**
     * @param userName
     * @param password
     * @param expired  设置多少秒之后过期
     * @return
     */
    public Map<String, Object> login(String userName, String password, int expired);

    /**
     * 退出功能业务
     *
     * @param ticket
     */
    public void exit(String ticket);

    /**
     * 根据cookie中的ticket查找登录凭证
     *
     * @param ticket
     * @return
     */
    public LoginTicket selectLoginTicket(String ticket);

    /**
     * 上传文件之后修改用户头像的路径
     *
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateUserHeaderUrl(String userId, String headerUrl);

    /**
     * 更新密码操作
     *
     * @param userId
     * @param password
     * @param newPassword
     * @return
     */
    public Map<String, Object> updateUserPassword(String userId, String password, String newPassword);


    /**
     * 获取缓存中的user对象
     *
     * @param userId
     * @return
     */
    public User getCache(int userId);

    /**
     * 如果缓存中没有数据 则初始化缓存
     *
     * @param userId
     * @return
     */
    public User initCache(int userId);

    /**
     * 清除缓存
     *
     * @param userId
     */
    public void clearCache(int userId);

    public Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
