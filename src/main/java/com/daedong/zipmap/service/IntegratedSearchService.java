package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.IntegratedSearchResponseDTO;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.mapper.PostMapper;
import com.daedong.zipmap.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegratedSearchService {

    private final ReviewMapper reviewMapper;
    private final PostMapper postMapper;

    /**
     * 통합 검색
     * @param keyword 검색어
     * @return 리뷰와 게시글이 포함된 통합 검색 DTO
     */

    @Transactional(readOnly = true)
    public IntegratedSearchResponseDTO searchAll(String keyword) {
        List<ReviewDTO> reviews = reviewMapper.findByKeywordReviews(keyword);
        List<PostDTO> posts = postMapper.findByKeywordPosts(keyword);

        return IntegratedSearchResponseDTO.builder()
                .reviews(reviews)
                .posts(posts)
                .keyword(keyword)
                .build();
    }

}
