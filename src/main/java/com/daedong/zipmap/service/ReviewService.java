package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.mapper.*;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final ProsMapper prosMapper;
    private final ConsMapper consMapper;
    private final FileUtilService fileUtilService;
    private final FileMapper fileMapper;
    private final RepliesMapper replyMapper;

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
            reviewDTO.setReplyList(replyMapper.getRepliesByTarget("review", id));

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

    public void deleteReviewById(Long id) {
        reviewMapper.deleteReviewById(id);
    }

    // 내가 쓴 리뷰 조회
    public Page<ReviewDTO> findMyReviews(Long userId, Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findByUserId(userId, pageable);
        int total = reviewMapper.countByUserId(userId);
        return new PageImpl<>(content, pageable, total);
    }

    // 내가 쓴 리뷰 댓글 조회
//    public Page<ReviewReply> findMyReplies(Long userId, Pageable pageable) {
//        List<ReviewReply> content = reviewReplyMapper.findByUserId(userId, pageable);
//        int total = reviewReplyMapper.countByUserId(userId);
//        return new PageImpl<>(content, pageable, total);
//    }

    @Cacheable(value = "mainReviewList")
    public List<ReviewDTO> getMainpageReview() {
        return reviewMapper.findOrderByCreatedAtDescLimit4();
    }

    // [추가] 글 내용(HTML)만 업데이트하는 메서드
    public void updateContent(Long id, String content) {
        // 매퍼에 쿼리 호출 (파라미터가 2개라 Map이나 @Param 필요할 수 있음)
        // 여기서는 편의상 DTO를 만들어서 보내거나, Map을 씁니다.
        // 가장 쉬운 방법: ReviewDTO를 재활용해서 보냅니다.
        ReviewDTO dto = new ReviewDTO();
        dto.setId(id);
        dto.setContent(content);
        reviewMapper.updateContent(dto);
        // 주의: Mapper XML에서 #{content}, #{id}를 쓰니까 DTO에 값이 있어야 함
    }

    /**
     * [실거주 인증] 사용자가 제출한 임대차계약서 파일을 서버에 저장하고 DB에 정보를 등록.
     * @param user 현재 로그인한 사용자 정보
     * @param file 사용자가 업로드한 MultipartFile 객체
     * @throws Exception 파일 저장 중 발생할 수 있는 오류 예외 처리
     */
    @Transactional
    public void registerCertification(User user, MultipartFile file, Long reviewId) throws IOException {

        // 1. 파일 유효성 검사
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다.");
        }

        // 2. 인증 정보(글) 먼저 저장 -> ID 생성됨
        Certification cert = new Certification();
        cert.setUserId(user.getId());
        cert.setReviewId(reviewId);

        reviewMapper.insertCertification(cert); // DB 저장 (ID 생성)


        // 3. ★ 파일 저장 (FileUtilService 이용)
        // "CERTIFICATION" 폴더에 저장 (c:/upload/certification/...)
        String filePath = fileUtilService.saveFile(file, "CERTIFICATION");


        // 4. ★ 공통 파일 테이블(file_attachment)에 저장
        FileAttachment attachment = new FileAttachment();
        attachment.setTargetType("CERTIFICATION");
        attachment.setTargetId(cert.getId()); // 방금 만든 인증 ID
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());

        fileMapper.insertAttachment(attachment);

        reviewMapper.updateReviewStatusToBanned(reviewId, "BANNED");
    }

}
