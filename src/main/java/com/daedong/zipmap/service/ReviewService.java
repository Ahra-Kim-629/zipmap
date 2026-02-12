package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Cons;
import com.daedong.zipmap.domain.Pros;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    // 페이징 조회
    public Page<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findAll(searchType, keyword, pros, cons, pageable);
        int total = reviewMapper.countTotal(searchType, keyword, pros, cons);
        return new PageImpl<>(content, pageable, total);
    }

    // 리뷰 작성
    @Transactional
    public long save(ReviewDTO reviewDTO) {
        reviewDTO.setReviewStatus("ACTIVE");
        Review review = new Review();
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setAddress(reviewDTO.getAddress());
        review.setUserId(reviewDTO.getUserId());
        review.setPoint(reviewDTO.getPoint());
        review.setReviewStatus(reviewDTO.getReviewStatus());
        reviewMapper.save(review);

        long savedId = review.getId();
        if (reviewDTO.getProsList() != null) {
            for (String attr : reviewDTO.getProsList()) {
                Pros pros = new Pros();
                pros.setReviewId(savedId);
                pros.setAttribute(attr);
                prosMapper.save(pros);
            }
        }
        if (reviewDTO.getConsList() != null) {
            for (String attr : reviewDTO.getConsList()) {
                Cons cons = new Cons();
                cons.setReviewId(savedId);
                cons.setAttribute(attr);
                consMapper.save(cons);
            }
        }

        return savedId;
    }

    // 리뷰 아이디로 찾기
    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {
        ReviewDTO reviewDTO = reviewMapper.findById(id);

        if (reviewDTO != null) {
            // 댓글 가져와서 끼워넣기
            reviewDTO.setReplyList(reviewReplyMapper.findByReviewId(id));
            // 파일 가져와서 끼워넣기
            reviewDTO.setFileList(fileMapper.findFilesByReviewId(id));

            // 2. 장점 처리 (객체 리스트 -> 문자열 리스트로 변환)
            List<Pros> prosEntities = prosMapper.findByReviewId(id);
            List<String> prosStrings = new ArrayList<>();
            for (Pros p : prosEntities) {
                prosStrings.add(p.getAttribute()); // 내용만 추출!
            }
            reviewDTO.setProsList(prosStrings);

            // 3. 단점 처리 (동일한 방식)
            List<Cons> consEntities = consMapper.findByReviewId(id);
            List<String> consStrings = new ArrayList<>();
            for (Cons c : consEntities) {
                consStrings.add(c.getAttribute()); // 내용만 추출!
            }
            reviewDTO.setConsList(consStrings);
        }

        return reviewDTO;
    }

    // 전체 조회 (지도용)
    public List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons) {
        return reviewMapper.findAll(searchType, keyword, pros, cons, null);
    }

    // 리뷰 수정
    @Transactional
    public void edit(ReviewDTO reviewDTO) {
        ReviewDTO review = reviewMapper.findById(reviewDTO.getId());

        if (review.getUserId() != reviewDTO.getUserId()) {
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
        if (reviewDTO.getProsList() != null) {
            for (String attr : reviewDTO.getProsList()) {
                Pros pros = new Pros();
                pros.setReviewId(reviewDTO.getId());
                pros.setAttribute(attr);
                prosMapper.save(pros);
            }
        }

        // 기존 단점 지우기
        consMapper.deleteByReviewId(reviewDTO.getId());
        // 새로 받은 단점 추가
        if (reviewDTO.getConsList() != null) {
            for (String attr : reviewDTO.getConsList()) {
                Cons cons = new Cons();
                cons.setReviewId(reviewDTO.getId());
                cons.setAttribute(attr);
                consMapper.save(cons);
            }
        }
    }

    @Cacheable(value = "mainReviewList")
    public List<ReviewDTO> getMainpageReview() {
        return reviewMapper.findOrderByCreatedAtDescLimit4();
    }
}
