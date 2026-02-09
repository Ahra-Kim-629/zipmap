package com.daedong.zipmap.repository;

import com.daedong.zipmap.domain.PostDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper {
    PostDTO findById(long id);
}
