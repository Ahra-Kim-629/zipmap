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
    }

    public void updateReply(ReviewReply reviewReply) {
    }

    public void deleteReply(Long replyId) {
    }

    public ReviewReply findReplyById(Long reviewId) {
    }
}
