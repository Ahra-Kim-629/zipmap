package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.ReviewReply;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Mapper
public interface ReviewMapper {
    ReviewDTO findById(Long id);

    List<ReviewReply> findReplyById(Long id);

    String findWriterById(Long id);

    List<Review> findAll(String searchType, String keyword, Pageable pageable);

    int countTotal(String searchType, String keyword);
}