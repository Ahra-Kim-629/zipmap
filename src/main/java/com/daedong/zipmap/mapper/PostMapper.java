package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface PostMapper {
    PostDTO findById(long id);

    List<Post> findAll(String searchType, String key);

    void save(Post post);

    List<Post> findAll(@Param("searchType") String searchType, @Param("keyword") String keyword, @Param("pageable") Pageable pageable);

    int countAll(@Param("searchType") String searchType, @Param("keyword") String keyword);

}
