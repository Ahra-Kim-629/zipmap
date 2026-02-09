package com.daedong.zipmap.postmapper;

import com.daedong.zipmap.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> findAll(@Param("searchType") String searchType, @Param("keyword") String keyword, @Param("pageable") Pageable pageable);
    int countAll(@Param("searchType") String searchType, @Param("keyword") String keyword);
}
