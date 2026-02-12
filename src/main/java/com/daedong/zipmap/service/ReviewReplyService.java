package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReviewReply;
import com.daedong.zipmap.mapper.ReviewReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<ReviewReply> findByReviewId(Long id) {
        return reviewReplyMapper.findByReviewId(id);
    }
}
