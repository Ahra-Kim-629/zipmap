package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    PostDTO findById(long id);

    List<Post> findAll(String searchType, String key);

    void save(Post post);

}
