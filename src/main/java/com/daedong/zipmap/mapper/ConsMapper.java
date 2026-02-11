package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Cons;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConsMapper {
    List<Cons> findByReviewId(Long id);

    void deleteByReviewId(long id);

    void add(Cons cons);
}
