package com.daedong.zipmap.repository;

import com.daedong.zipmap.domain.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper {

    Post findById(long id);
}
