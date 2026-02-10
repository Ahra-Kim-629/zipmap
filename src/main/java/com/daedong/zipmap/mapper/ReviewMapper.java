package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.ReviewReply;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Mapper
public interface ReviewMapper {
    List<Review> findAll(String searchType, String keyword, Pageable pageable);

    int countTotal(String searchType, String keyword);

    // 리뷰 아이디로 찾기
    Review findById(Long id);

    // 리뷰 작성
    void save(Review review);

    // 리뷰 수정
    void edit(Review review);

}