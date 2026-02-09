package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Review;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface ReviewMapper {

    List<Review> findAll(String searchType, String keyword, Pageable pageable);

    int countTotal(String searchType, String keyword);
}