package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Cons;

import java.util.List;

public interface ConsMapper {
    List<Cons> findByReviewId(Long id);

    void deleteByReviewId(long id);

    void add(Cons cons);
}
