package com.daedong.zipmap.domain.search.service;

import com.daedong.zipmap.domain.search.dto.IntegratedSearchResponseDTO;
import com.daedong.zipmap.domain.post.dto.PostDTO;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.post.mapper.PostMapper;
import com.daedong.zipmap.domain.review.mapper.ReviewMapper;
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
