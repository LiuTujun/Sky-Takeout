package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据OpenId获取用户
     * @return
     */
    @Select("select * from user where openid=#{openId}")
    User getByOpenId(String openId);

    void insert(User newUser);
}
