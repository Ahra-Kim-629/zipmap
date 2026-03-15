package com.daedong.zipmap.domain.review.service;

import com.daedong.zipmap.domain.interaction.reaction.service.ReactionService;
import com.daedong.zipmap.domain.member.entity.User;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.review.entity.Certification;
import com.daedong.zipmap.domain.review.entity.Review;
import com.daedong.zipmap.domain.review.entity.ReviewAttribute;
import com.daedong.zipmap.global.common.enums.Status;
import com.daedong.zipmap.global.file.entity.File;
import com.daedong.zipmap.global.file.mapper.FileMapper;
import com.daedong.zipmap.domain.review.mapper.ReviewMapper;
import com.daedong.zipmap.global.file.service.FileUtilService;
import com.daedong.zipmap.global.util.NetworkUtil;
import com.daedong.zipmap.domain.interaction.reply.service.ReplyService;
import com.daedong.zipmap.domain.interaction.common.StatsUtil;
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
        if (reviewDTO == null) return null;

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
    @CacheEvict(value = {"aiSummaryCache", "mainReviewList"}, allEntries = true)
    @Transactional
    public long write(Review review, List<String> prosList, List<String> consList) {

        // ✨ 추가: 리뷰 작성 시 무조건 '대기중(PENDING)' 상태로 세팅
        review.setReviewStatus(Status.PENDING);

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

        // 2/26 추가 4. 대표 사진(썸네일) 처리 로직 추가 (DB 추가 없이 file 테이블 활용)
        String thumbnail = review.getThumbnailPath();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // 넘어온 경로의 /temp/ 부분을 정식 폴더명(소문자 review)으로 치환
            String finalThumbnail = thumbnail.replace("/files/temp/", "review/");

            // file 테이블에 'REVIEW_THUMB' 이라는 타입으로 한 줄 저장
            reviewMapper.insertThumbnail(reviewId, finalThumbnail);
        }



        return reviewId;
    }

    // 리뷰 수정
    @CacheEvict(value = {"aiSummaryCache", "mainReviewList"}, allEntries = true)
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

        // 2/26 썸네일(대표 사진) 처리 로직 추가
        String thumbnail = review.getThumbnailPath();

        // 1. 사용자가 사진을 바꿨든, 아예 다 지웠든 상관없이 일단 기존 대표 사진 기록은 무조건 날림
        fileUtilService.deleteFilesByTargetTypeAndTargetId("REVIEW_THUMB", reviewId);

        if (thumbnail != null && !thumbnail.isEmpty()) {

            // 2. 임시 경로 또는 기존 경로를 정식 경로 포맷으로 변환
            String finalThumbnail = thumbnail.replace("/files/temp/", "review/");

            // 3. file 테이블에 새로운 대표 사진 저장
            reviewMapper.insertThumbnail(reviewId, finalThumbnail);
        }
    }

//    // 리뷰 status 수정 (certification)
//    @Transactional // 👈 중요: 두 테이블 수정을 하나의 작업으로 묶습니다.
//    public void changeReviewAndCertificationStatus(Long reviewId, Status status) {
//        // 1. 리뷰 테이블 상태 업데이트
//        reviewMapper.updateReviewStatus(reviewId, status);
//
//        // 2. 만약 상태를 ACTIVE(승인)로 바꾸는 것이라면, 인증 테이블도 함께 업데이트
//        if (status == Status.ACTIVE) {
//            reviewMapper.updateCertificationStatus(reviewId, Status.ACTIVE);
//        }
//    }

    // 리뷰 삭제
    @CacheEvict(value = {"aiSummaryCache", "mainReviewList"}, allEntries = true)
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

        // 2. 새로운 인증 정보 저장
        Certification cert = new Certification();
        cert.setUserId(user.getId());
        cert.setReviewId(reviewId);

        // 2/24 추가: 인증 객체에도 초기 상태값을 명시적으로 세팅해 줍니다!
        cert.setCertificationStatus(Status.PENDING);


        reviewMapper.insertCertification(cert); // DB 저장 (ID 생성)


        // 3. ★ 파일 저장 (FileUtilService 이용)
        // "CERTIFICATION" 폴더에 저장 (c:/upload/certification/...)
        String filePath = fileUtilService.saveFile(image, "certification");

        // 4. ★ 공통 파일 테이블(file_attachment)에 저장
        File file = new File();
        file.setTargetType("certification");
        file.setTargetId(cert.getId()); // 방금 만든 인증 ID
        file.setFilePath(filePath);
        file.setFileSize(image.getSize());
        fileUtilService.saveFileToDB(file);

        // ✅ 수정 후 (메서드 이름도 범용적으로 번경, 상태도 PENDING으로 올바르게 수정)
        reviewMapper.updateReviewStatus(reviewId, Status.PENDING);
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

    @Transactional
    public void processCertification(Long reviewId, Status status, String message) {
        // 1. 리뷰 상태 변경 (ACTIVE 또는 REJECTED 등)
        // 반려 시 아예 리스트에서 안 보이게 하려면 REJECTED나 BANNED로 변경
        reviewMapper.updateReviewStatus(reviewId, status);

        // 2. 인증(Certification) 테이블 상태 및 메시지 업데이트
        // 해당 리뷰의 가장 최근 PENDING 상태인 인증 데이터를 찾아서 업데이트
        reviewMapper.updateCertificationResult(reviewId, status, message);
    }

}
