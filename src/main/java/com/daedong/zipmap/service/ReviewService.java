package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Certification;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.User;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Transactional
    public ReviewDTO getReviewDetail(Long id, HttpServletRequest request, UserDetails userDetails) {
        ReviewDTO reviewDTO = reviewMapper.findById(id);

        // 조회수 증가 위한 redis 처리
        if (reviewDTO != null) {
            // 로그인 했으면 userId 를, 안했으면 IP 를 식별자로 보내줌
            String identifier = (userDetails != null) ? userDetails.getUsername() : NetworkUtil.getClientIp(request);

            // 예외 처리를 추가하여 Redis 장애가 게시글 조회를 막지 않도록 보호
            try {
                statsUtil.updateViewCount("review", id, identifier);
            } catch (Exception e) {
                System.out.println("Redis 카운트 증가 실패 (게시글 ID: {" + id + "}): {" + e.getMessage() + "}");
            }
        }

        // 좋아요 표시
        reviewDTO.setLikeCount(reactionService.countReaction("review", id, 1));

        // 파일 리스트 추가
        reviewDTO.setFileList(fileUtilService.getFileList("review", id));

        // 댓글 리스트 추가
        reviewDTO.setReplyList(replyService.getReplyDTOList("review", id));

        crimeStatsService.analyzeCrimeForReview(reviewDTO);

        return reviewDTO;
    }

    public ReviewDTO getReviewDetail(long id) {
        return reviewMapper.findById(id);
    }

    // 전체 조회 (지도용)
    public List<ReviewDTO> findAll(String searchType, String keyword, List<String> pros, List<String> cons) {
        return reviewMapper.findAll(searchType, keyword, pros, cons, null);
    }

    // 리뷰 작성
    @CacheEvict(value = "aiSummaryCache", allEntries = true)
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
    @CacheEvict(value = "aiSummaryCache", allEntries = true)
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
    @CacheEvict(value = "aiSummaryCache", allEntries = true)
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

    // 💡 [AI 요약용] 특정 지역의 리뷰 내용만 DB에서 가져오는 기능
    public List<String> findContentsByRegion(String region) {
        return reviewMapper.findContentsByRegion(region);
    }

}
