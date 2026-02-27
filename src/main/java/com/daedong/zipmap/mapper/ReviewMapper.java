package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.*;
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
    void insertReview(Review review);

    void insertAttributesAll(long reviewId, String type, List<String> attributeList);

    // 리뷰 수정
    void update(Review review);

    // 리뷰 삭제
    void deleteReviewById(Long id);

    // 내가 쓴 리뷰 조회
    List<ReviewDTO> findByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

    int countByUserId(@Param("userId") Long userId);

    List<ReviewDTO> findOrderByCreatedAtDescLimit4();

    void updateContent(long id, String content);

    void deleteAttributeByReviewId(long id);

    void deleteReplyByReviewId(Long id);

    void deleteReactionByReviewId(Long id);

    /**
     * [실거주 인증] 사용자가 올린 인증 서류 정보를 DB에 저장
     * 2026.02.12 실거주 인증 기능 추가
     */
    void insertCertification(Certification certification);


    // 2-24 수정
    // void updateReviewStatusToBanned(@Param("reviewId") Long reviewId, @Param("status") String status);

    void updateReviewStatus(@Param("reviewId") Long reviewId, @Param("status") Status status);


    List<ReviewDTO> adminFindAll(@Param("searchType") String searchType,
                                 @Param("keyword") String keyword,
                                 @Param("pageable") Pageable pageable);

    int adminCountTotal(@Param("searchType") String searchType,
                        @Param("keyword") String keyword);

    // admin 에서 BANNED된 리뷰 목록 볼수 있도록 추가
    List<ReviewDTO> findBannedReviews(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int countBannedReviews();

    void updateReviewStatsBatch(List<StatsUpdateDTO> updateList);

    // AI 요약을 위해 특정 지역의 리뷰 내용만 모두 가져오는 메서드 2/23 추가
    List<String> findContentsByRegion(String region);

    List<ReviewAttribute> findAttributesByReviewId(long id);

    List<ReviewDTO> findByKeywordReviews(@Param("keyword") String keyword);

    int countPendingCertifications();


    //  2/26 추가: 대표 사진(썸네일)을 file 테이블에 'REVIEW_THUMB' 타입으로 저장
    void insertThumbnail(@Param("targetId") long targetId, @Param("filePath") String filePath);

    // 리뷰 인증 관련 ,
    void updateCertificationStatus(@Param("reviewId") Long reviewId, @Param("status") Status status);}
