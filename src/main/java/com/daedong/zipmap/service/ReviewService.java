package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    // 페이징 조회
    public Page<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findAll(searchType, keyword, pros, cons, pageable);
        int total = reviewMapper.countTotal(searchType, keyword, pros, cons);
        return new PageImpl<>(content, pageable, total);
    }

    // 전체 조회 (지도용)
    public List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons) {
        return reviewMapper.findAll(searchType, keyword, pros, cons, null);
    }
}
