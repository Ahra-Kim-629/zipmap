package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.ReviewReply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewReplyMapper {
    List<ReviewReply> findByReviewId(Long id);
}
