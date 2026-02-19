package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Certification;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Mapper
public interface ReviewMapper {
    List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons, Pageable pageable);

    int countTotal(String searchType, String keyword, List<String> pros, List<String> cons);

    // 리뷰 아이디로 찾기
    ReviewDTO findById(Long id);

    // 리뷰 작성
    void save(Review review);

    // 리뷰 수정
    void edit(ReviewDTO reviewDTO);

    // 리뷰 삭제
    void deleteReviewById(Long id);

    // 내가 쓴 리뷰 조회
    List<ReviewDTO> findByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);
    int countByUserId(@Param("userId") Long userId);

    List<ReviewDTO> findOrderByCreatedAtDescLimit4();

    // 추가 2/13
    void updateContent(ReviewDTO dto);

    /**
     * [실거주 인증] 사용자가 올린 인증 서류 정보를 DB에 저장
     * 2026.02.12 실거주 인증 기능 추가
     */
    void insertCertification(Certification certification);

    void updateReviewStatusToBanned(Long reviewId, String banned);
}
