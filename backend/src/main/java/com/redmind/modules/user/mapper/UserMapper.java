package com.redmind.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redmind.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from users where email = #{email} limit 1")
    User findByEmail(String email);
}
