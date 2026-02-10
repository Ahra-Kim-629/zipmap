package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Pros;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProsMapper {
    List<Pros> findByReviewId(Long id);

    void deleteByReviewId(long id);

    void add(Pros pros);
}
