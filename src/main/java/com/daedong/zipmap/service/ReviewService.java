package com.daedong.zipmap.service;


import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;

    public Page<Review> findAll(String searchType, String keyword, Pageable pageable) {
        int total = reviewMapper.countTotal(searchType, keyword);
        List<Review> list = reviewMapper.findAll(searchType, keyword, pageable);
        return new PageImpl<>(list, pageable, total);

    }
}
