package com.mengnankk.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengnankk.user.entry.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
