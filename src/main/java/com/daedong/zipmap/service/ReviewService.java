package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Certification;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.FileMapper;
import com.daedong.zipmap.mapper.ReplyMapper;
import com.daedong.zipmap.mapper.ReviewMapper;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.util.NetworkUtil;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final FileMapper fileMapper;
    private final ReplyMapper replyMapper;
    private final FileUtilService fileUtilService;
    private final StatsUtil statsUtil;


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
    public ReviewDTO findById(Long id, HttpServletRequest request, UserDetails userDetails) {
        ReviewDTO reviewDTO = reviewMapper.getReviewDetail(id);

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
        return reviewMapper.getReviewDetail(id);
    }

    public ReviewDTO findById(long id) {
        return reviewMapper.findById(id);
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

    // [추가] 글 내용(HTML)만 업데이트하는 메서드
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

    /**
     * [실거주 인증] 사용자가 제출한 임대차계약서 파일을 서버에 저장하고 DB에 정보를 등록.
     *
     * @param user  현재 로그인한 사용자 정보
     * @param image 사용자가 업로드한 MultipartFile 객체
     * @throws Exception 파일 저장 중 발생할 수 있는 오류 예외 처리
     */
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

    // 2. 클래스 내부에 아래 메서드 추가
    public int countTotal(String searchType, String keyword, List<String> pros, List<String> cons) {
        // 서비스는 단순히 매퍼에게 일을 시키는 '징검다리' 역할을 합니다.
        return reviewMapper.countTotal(searchType, keyword, pros, cons);
    }

    // ReviewService.java 내에 추가
    public Page<ReviewDTO> adminFindAll(String searchType, String keyword, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.adminFindAll(searchType, keyword, pageable);
        int total = reviewMapper.adminCountTotal(searchType, keyword);
        return new PageImpl<>(content, pageable, total);
    }

}
