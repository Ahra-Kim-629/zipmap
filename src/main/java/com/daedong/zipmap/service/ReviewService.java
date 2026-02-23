package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.mapper.FileMapper;
import com.daedong.zipmap.mapper.ReviewMapper;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.util.NetworkUtil;
import com.daedong.zipmap.util.ReplyService;
import com.daedong.zipmap.util.StatsUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final FileMapper fileMapper;
    private final FileUtilService fileUtilService;
    private final StatsUtil statsUtil;
    private final ReactionService reactionService;
    private final ReplyService replyService;
    private final CrimeStatsService crimeStatsService;

    // 페이징 조회
    public Page<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findAll(searchType, keyword, pros, cons, pageable);
        int total = reviewMapper.countTotal(searchType, keyword, pros, cons);
        return new PageImpl<>(content, pageable, total);
    }

    // 리뷰 아이디로 찾기
    public ReviewDTO getReviewDetail(long id) {
        ReviewDTO reviewDTO = reviewMapper.findById(id);
        if (reviewDTO != null) {
            fillReviewRelatedData(reviewDTO);
        }
        return reviewDTO;
    }

    // 리뷰 아이디로 찾기
    @Transactional
    public ReviewDTO getReviewDetail(Long id, HttpServletRequest request, UserDetails userDetails) {
        ReviewDTO reviewDTO = reviewMapper.findById(id);
        if(reviewDTO == null) return null;

        // 외부 연동 데이터 처리 (Redis 조회수)
        // 조회수는 '검증'과 '외부 저장소' 접근이 필요하므로 서비스에서 호출하는 것이 적절
        handleViewCount(reviewDTO, request, userDetails);

        // 추가 데이터 (좋아요, 파일, 댓글, 통계)
        fillReviewRelatedData(reviewDTO);

        return reviewDTO;
    }

    // 추가 데이터 채우는 메서드
    private void fillReviewRelatedData(ReviewDTO reviewDTO) {
        long id = reviewDTO.getId();

        // 좋아요, 파일, 댓글 리스트
        reviewDTO.setLikeCount(reactionService.countReaction("review", id, 1));
        reviewDTO.setFileList(fileUtilService.getFileList("review", id));
        reviewDTO.setReplyList(replyService.getReplyDTOList("review", id));

        reviewDTO.setProsList(new ArrayList<>());
        reviewDTO.setConsList(new ArrayList<>());
        // 장단점
        List<ReviewAttribute> attributes = reviewMapper.findAttributesByReviewId(id);

        List<String> pros = new ArrayList<>();
        List<String> cons = new ArrayList<>();

        for (ReviewAttribute attr : attributes) {
            if ("PRO".equals(attr.getType())) {
                pros.add(attr.getContent());
            } else if ("CON".equals(attr.getType())) {
                cons.add(attr.getContent());
            }
        }
        reviewDTO.setProsList(pros);
        reviewDTO.setConsList(cons);

        // 범죄 통계 분석
        crimeStatsService.analyzeCrimeForReview(reviewDTO);
    }

    // 조회수 로직 분리
    private void handleViewCount(ReviewDTO reviewDTO, HttpServletRequest request, UserDetails userDetails) {
        String identifier = (userDetails != null) ? userDetails.getUsername() : NetworkUtil.getClientIp(request);
        try {
            statsUtil.updateViewCount("review", reviewDTO.getId(), identifier);
        } catch (Exception e) {
            System.out.println("Redis 조회수 증가 실패 : " + e.getMessage());
        }
    }

    // 전체 조회 (지도용)
    public List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons) {
        return reviewMapper.findAll(searchType, keyword, pros, cons, null);
    }

    // 리뷰 작성
    @Transactional
    public long write(Review review, List<String> prosList, List<String> consList) {
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

    // 리뷰 삭제
    @Transactional
    public void deleteReviewById(Long id) {
        // 댓글 삭제
        replyService.deleteByTargetTypeAndTargetId("review", id);
        // 리액션 삭제
        reactionService.deleteByTargetTypeAndTargetId("review", id);
        // 파일 삭제
        fileUtilService.deleteFilesByTargetTypeAndTargetId("review", id);
        // 속성 삭제
        reviewMapper.deleteAttributeByReviewId(id);
        // 리뷰 삭제
        reviewMapper.deleteReviewById(id);

        fileUtilService.deleteFilesByTargetTypeAndTargetId("REVIEW", id);
    }

    // 내가 쓴 리뷰 조회
    public Page<ReviewDTO> getMyReviews(Long userId, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findByUserId(userId, pageable);
        int total = reviewMapper.countByUserId(userId);
        return new PageImpl<>(content, pageable, total);
    }

    @Cacheable(value = "mainReviewList")
    public List<ReviewDTO> getMainpageReview() {
        List<ReviewDTO> reviewDTOList = reviewMapper.findOrderByCreatedAtDescLimit4();
        for (ReviewDTO review : reviewDTOList) {
            // HTML 태그 제거
            String cleanText = Jsoup.clean(review.getContent(), Safelist.none());

            // HTML 엔티티 (&lt, &gt) 를 실제 기호 (<, >)로 변환
            cleanText = Jsoup.parse(cleanText).text();

            review.setContent(cleanText);
        }
        return reviewDTOList;
    }

    // 실거주 인증
    @Transactional
    public void registerCertification(User user, MultipartFile image, Long reviewId) throws IOException {

        // 1. 파일 유효성 검사
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다.");
        }

        // 2. 인증 정보(글) 먼저 저장 -> ID 생성됨
        Certification cert = new Certification();
        cert.setUserId(user.getId());
        cert.setReviewId(reviewId);

        reviewMapper.insertCertification(cert); // DB 저장 (ID 생성)


        // 3. ★ 파일 저장 (FileUtilService 이용)
        // "CERTIFICATION" 폴더에 저장 (c:/upload/certification/...)
        String filePath = fileUtilService.saveFile(image, "CERTIFICATION");


        // 4. ★ 공통 파일 테이블(file_attachment)에 저장
        com.daedong.zipmap.domain.File file = new com.daedong.zipmap.domain.File();
        file.setTargetType("CERTIFICATION");
        file.setTargetId(cert.getId()); // 방금 만든 인증 ID
        file.setFilePath(filePath);
        file.setFileSize(image.getSize());

        fileMapper.insertFile(file);

        reviewMapper.updateReviewStatusToBanned(reviewId, "BANNED");
    }

    public int countTotal(String searchType, String keyword, List<String> pros, List<String> cons) {
        // 서비스는 단순히 매퍼에게 일을 시키는 '징검다리' 역할을 합니다.
        return reviewMapper.countTotal(searchType, keyword, pros, cons);
    }

    public Page<ReviewDTO> adminFindAll(String searchType, String keyword, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.adminFindAll(searchType, keyword, pageable);
        int total = reviewMapper.adminCountTotal(searchType, keyword);
        return new PageImpl<>(content, pageable, total);
    }

}
