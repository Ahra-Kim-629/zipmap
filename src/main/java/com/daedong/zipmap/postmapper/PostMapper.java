package com.daedong.zipmap.postmapper;

import com.daedong.zipmap.postdomain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> findAll();

    void save(Post post);

}
