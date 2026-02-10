package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReviewReply;
import com.daedong.zipmap.mapper.ReviewReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewReplyService {
    private final ReviewReplyMapper reviewReplyMapper;


    public void addReply(ReviewReply reviewReply) {
        reviewReplyMapper.addReply(reviewReply);
    }

    public void updateReply(ReviewReply reviewReply) {
        reviewReplyMapper.updateReply(reviewReply);
    }

    public void deleteReply(long reviewReplyId) {
        reviewReplyMapper.deleteReply(reviewReplyId);
    }

    public ReviewReply findReplyById(Long reviewId) {
        return reviewReplyMapper.findReplyById(reviewId);
    }
}
