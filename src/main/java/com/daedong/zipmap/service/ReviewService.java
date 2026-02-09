package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewReply;
import com.daedong.zipmap.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;

    public Review findById(Long id) {
        return reviewMapper.findById(id);
    }

    public List<ReviewReply> findReplyById(Long id) {
        return reviewMapper.findReplyById(id);
    }


    public String findWriterById(Long id) {
        return reviewMapper.findWriterById(id);
    }

    public void save(Review review) {
    }
}
