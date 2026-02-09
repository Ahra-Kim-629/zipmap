package com.daedong.zipmap.postmapper;

import com.daedong.zipmap.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> findAll(@Param("searchType") String searchType, @Param("key") String key);
}
