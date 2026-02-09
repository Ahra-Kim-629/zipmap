package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByLoginId(String loginId);
}
