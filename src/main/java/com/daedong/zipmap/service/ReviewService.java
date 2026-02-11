package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final ProsMapper prosMapper;
    private final ConsMapper consMapper;
    private final FileMapper fileMapper;
    private final ReviewReplyMapper reviewReplyMapper;

    public Page<Review> findAll(String searchType, String keyword, Pageable pageable) {
        int total = reviewMapper.countTotal(searchType, keyword);
        List<Review> list = reviewMapper.findAll(searchType, keyword, pageable);
        return new PageImpl<>(list, pageable, total);

    }

    // 리뷰 작성
    @Transactional
    public long save(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setAddress(reviewDTO.getAddress());
        review.setUserId(reviewDTO.getUserId());
        review.setPoint(reviewDTO.getPoint());
        review.setReviewStatus(reviewDTO.getReviewStatus());
        reviewMapper.save(review);

        long savedId = review.getId();
        if(reviewDTO.getProsList() != null){
            for(String attr : reviewDTO.getProsList()){
                Pros pros = new Pros();
                pros.setReviewId(savedId);
                pros.setAttribute(attr);
                prosMapper.add(pros);
            }
        }

        return savedId;
    }

    // 리뷰 아이디로 찾기
    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {
        Review review = reviewMapper.findById(id);

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setTitle(review.getTitle());
        reviewDTO.setContent(review.getContent());
        reviewDTO.setAddress(review.getAddress());
        reviewDTO.setUserId(review.getUserId());
        reviewDTO.setPoint(review.getPoint());
        reviewDTO.setReviewStatus(review.getReviewStatus());
        reviewDTO.setCreatedAt(review.getCreatedAt());
        reviewDTO.setUpdatedAt(review.getUpdatedAt());

        // 장점 처리
        List<Pros> prosEntities = prosMapper.findByReviewId(id);
        List<String> prosStrings = new ArrayList<>();

        for (Pros p : prosEntities){
            prosStrings.add(p.getAttribute());
        }
        reviewDTO.setProsList(prosStrings);

        // 단점 처리
        List<Cons> consEntities = consMapper.findByReviewId(id);
        List<String> consStrings = new ArrayList<>();

        for (Cons c : consEntities) {
            consStrings.add(c.getAttribute());
        }
        reviewDTO.setConsList(consStrings);

        reviewDTO.setFileList(fileMapper.findByReviewId(id));
        reviewDTO.setReplyList(reviewReplyMapper.findReplyById(id));

        return reviewDTO;
    }

    // 리뷰 수정
    @Transactional
    public void edit(ReviewDTO reviewDTO) {
        Review review = reviewMapper.findById(reviewDTO.getId());

        if(review.getUserId() != reviewDTO.getUserId()){
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setAddress(reviewDTO.getAddress());
        review.setPoint(reviewDTO.getPoint());
        review.setReviewStatus(reviewDTO.getReviewStatus());

        reviewMapper.edit(review);

        // 기존 장점 지우기
        prosMapper.deleteByReviewId(reviewDTO.getId());
        // 새로 받은 장점 추가
        if(reviewDTO.getProsList() != null){
            for(String attr : reviewDTO.getProsList()){
                Pros pros = new Pros();
                pros.setReviewId(reviewDTO.getId());
                pros.setAttribute(attr);
                prosMapper.add(pros);
            }
        }

        // 기존 단점 지우기
        consMapper.deleteByReviewId(reviewDTO.getId());
        // 새로 받은 단점 추가
        if(reviewDTO.getConsList() != null){
            for(String attr : reviewDTO.getConsList()) {
                Cons cons = new Cons();
                cons.setReviewId(reviewDTO.getId());
                cons.setAttribute(attr);
                consMapper.add(cons);
            }
        }

    }


}
