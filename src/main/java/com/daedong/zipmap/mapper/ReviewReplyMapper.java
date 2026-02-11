package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.ReviewReply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewReplyMapper {
    // 댓글 작성
    void addReply(ReviewReply reviewReply);

    // 댓글 조회
    ReviewReply findReplyById(long id);

    // 댓글 수정
    void updateReply(ReviewReply reviewReply);

    // 댓글 삭제
<<<<<<< HEAD
    void deleteReply(long reviewReplyId);
=======

    List<ReviewReply> findByReviewId(Long id);
>>>>>>> c02d284fa9e2e837ae11e15673c3f14a7cd75889
}
