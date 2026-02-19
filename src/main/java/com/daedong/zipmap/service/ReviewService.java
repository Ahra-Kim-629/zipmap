package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.mapper.FileMapper;
import com.daedong.zipmap.mapper.ReplyMapper;
import com.daedong.zipmap.mapper.ReviewMapper;
import com.daedong.zipmap.mapper.ReviewReplyMapper;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final FileMapper fileMapper;
    private final ReplyMapper replyMapper;
    private final ReviewReplyMapper reviewReplyMapper;
    private final FileUtilService fileUtilService;


    // 페이징 조회
    public Page<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findAll(searchType, keyword, pros, cons, pageable);
        int total = reviewMapper.countTotal(searchType, keyword, pros, cons);
        return new PageImpl<>(content, pageable, total);
    }

    // 리뷰 작성
    @Transactional
    public long save(Review review, List<String> prosList, List<String> consList) {
        reviewMapper.insertReview(review);
        long reviewId = review.getId();

        if (prosList != null && !prosList.isEmpty()) {
            reviewMapper.insertAttributesAll(reviewId, "PRO", prosList);
        }

        if (consList != null && !consList.isEmpty()) {
            reviewMapper.insertAttributesAll(reviewId, "CON", consList);
        }

        // 리턴값(newContent): 이미지 경로가 "/files/review/..."로 변경된 HTML
        String newContent = fileUtilService.moveTempFilesToPermanent(review.getContent(), "REVIEW", reviewId);

        // 3. 바뀐 HTML 내용으로 DB 업데이트
        reviewMapper.updateContent(reviewId, newContent);
        return reviewId;
    }

    // 리뷰 아이디로 찾기
    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {

        return reviewMapper.getReviewDetail(id);
    }

    // 전체 조회 (지도용)
    public List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons) {
        return reviewMapper.findAll(searchType, keyword, pros, cons, null);
    }

    // 리뷰 수정
    @Transactional
    public void update(Review review, List<String> prosList, List<String> consList) {
        reviewMapper.update(review);

        long reviewId = review.getId();
        reviewMapper.deleteAttributeByReviewId(reviewId);
        if (prosList != null && !prosList.isEmpty()) {
            reviewMapper.insertAttributesAll(reviewId, "PRO", prosList);
        }

        if (consList != null && !consList.isEmpty()) {
            reviewMapper.insertAttributesAll(reviewId, "CON", consList);
        }

        // 리턴값(newContent): 경로가 정리된 HTML
        String newContent = fileUtilService.updateImagesFromContent(review.getContent(), "REVIEW", reviewId);

        // 바뀐 HTML 내용으로 DB 업데이트
        reviewMapper.updateContent(reviewId, newContent);
    }

    @Transactional
    public void deleteReviewById(Long id) {
        reviewMapper.deleteReviewById(id);
        reviewMapper.deleteReplyByReviewId(id);
        reviewMapper.deleteReactionByReviewId(id);
        reviewMapper.deleteAttributeByReviewId(id);
        fileUtilService.deleteFilesByTargetTypeAndTargetId("REVIEW", id);
    }
//
//    // 내가 쓴 리뷰 조회
//    public Page<ReviewDTO> findMyReviews(Long userId, Pageable pageable) {
//        List<ReviewDTO> content = reviewMapper.findByUserId(userId, pageable);
//        int total = reviewMapper.countByUserId(userId);
//        return new PageImpl<>(content, pageable, total);
//    }
//
//    // 내가 쓴 리뷰 댓글 조회
//    public Page<ReviewReply> findMyReplies(Long userId, Pageable pageable) {
//        List<ReviewReply> content = reviewReplyMapper.findByUserId(userId, pageable);
//        int total = reviewReplyMapper.countByUserId(userId);
//        return new PageImpl<>(content, pageable, total);
//    }
//
//    @Cacheable(value = "mainReviewList")
//    public List<ReviewDTO> getMainpageReview() {
//        return reviewMapper.findOrderByCreatedAtDescLimit4();
//    }
//
//    // [추가] 글 내용(HTML)만 업데이트하는 메서드
//    public void updateContent(Long id, String content) {
//        // 매퍼에 쿼리 호출 (파라미터가 2개라 Map이나 @Param 필요할 수 있음)
//        // 여기서는 편의상 DTO를 만들어서 보내거나, Map을 씁니다.
//        // 가장 쉬운 방법: ReviewDTO를 재활용해서 보냅니다.
//        ReviewDTO dto = new ReviewDTO();
//        dto.setId(id);
//        dto.setContent(content);
//        reviewMapper.updateContent(dto);
//        // 주의: Mapper XML에서 #{content}, #{id}를 쓰니까 DTO에 값이 있어야 함
//    }

}
