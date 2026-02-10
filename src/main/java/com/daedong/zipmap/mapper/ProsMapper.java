package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Pros;

import java.util.List;

public interface ProsMapper {
    List<Pros> findByReviewId(Long id);

    void deleteByReviewId(long id);

    void add(Pros pros);
}
