package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewReply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {
    Review findById(Long id);

    List<ReviewReply> findReplyById(Long id);

    String findWriterById(Long id);
}
