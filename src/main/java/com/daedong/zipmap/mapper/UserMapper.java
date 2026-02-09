package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findByNameAndEmail(String name, String email);

    Optional<User> findById(long id);
}
