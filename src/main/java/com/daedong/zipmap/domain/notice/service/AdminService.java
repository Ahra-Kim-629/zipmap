package com.daedong.zipmap.domain.notice.service;

import com.daedong.zipmap.domain.notice.entity.Notice;
import com.daedong.zipmap.domain.notice.dto.NoticeDTO;
import com.daedong.zipmap.domain.notice.mapper.NoticeMapper;
import com.daedong.zipmap.domain.post.mapper.PostMapper;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.review.mapper.ReviewMapper;
import com.daedong.zipmap.global.common.enums.Status;
import com.daedong.zipmap.global.file.entity.File;
import com.daedong.zipmap.global.file.mapper.FileMapper;
import com.daedong.zipmap.domain.subscription.service.AlarmService;
import com.daedong.zipmap.global.file.service.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final NoticeMapper noticeMapper;
    private final PostMapper postMapper;
    private final ReviewMapper reviewMapper;
    private final FileUtilService fileUtilService;
    private final FileMapper fileMapper;
    private final AlarmService alarmService;

    //  공지사항 리스트 페이징 처리
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticeAll(Pageable pageable) {
        // 1. 현재 페이지에 해당하는 8개만 쏙 빼오기
        List<NoticeDTO> content = noticeMapper.findNoticeAllWithPaging(
                pageable.getPageSize(),
                (int) pageable.getOffset()
        );

        // 2. 전체 공지사항이 몇 개인지 카운트하기
        int total = noticeMapper.countAllNotices();

        // 3. 페이지 객체로 예쁘게 포장해서 리턴
        return new PageImpl<>(content, pageable, total);
    }

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void insertNotice(Notice notice, MultipartFile imageFile) throws IOException {

        // 1. 먼저 공지사항 글부터 저장 (그래야 ID가 생김)
        notice.setStatus("Y");
        notice.setEndDate(notice.getEndDate().plusDays(1));
        noticeMapper.insertNotice(notice);

        // 2. 파일이 있으면 -> 디스크 저장 -> 'file' 테이블에 저장
        if (imageFile != null && !imageFile.isEmpty()) {

            // (1) 실제 폴더(c:/upload/notice)에 파일 저장
            String filePath = fileUtilService.saveFile(imageFile, "notice");

            // (2) ★ [핵심] 공통 파일 테이블(file)에 정보 저장!
            File file = new File();
            file.setTargetType("NOTICE");        // 구분값
            file.setTargetId(notice.getId());    // 방금 만든 공지사항 ID
            file.setFilePath(filePath);          // notice/uuid_파일명.jpg
            file.setFileSize(imageFile.getSize());

            fileMapper.insertFile(file);
        }
        noticeMapper.updateNotice(notice);
    }

    // 전체 회원 리스트 가져오기 2026.02.11 종빈 수정, -> 이 부분은 USER에서 기능 구현을 하는 것이 좋을 거 같아서 이동시켰습니다.
    /* @Transactional
    public List<User> findAllUsers() {
        return userMapper.findAllUsers(); // 매퍼 호출
    }

    @Transactional
    public void updateAccountStatus(long id, String status, String role) {
        User user = new User();
        user.setId(id);
        // 2/24 수정
        // user.setAccountStatus(status);
        // user.setRole(role);
        user.setAccountStatus(Status.valueOf(status));
        user.setRole(UserRole.valueOf(role));
        userMapper.updateUserStatusAndRole(user);
    }
    */

    @Cacheable(value = "mainNotices")
    public List<NoticeDTO> getCurrentNoticeList() {
        return noticeMapper.findCurrentNoticeList();
    }

//    public List<Post> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
//        return postMapper.adminFindAll(searchType, keyword, category, location, pageable);
//    }
//
//    public int getTotalCount(String searchType, String keyword, String category, String location) {
//        return postMapper.countAll(searchType, keyword, category, location);
//    }

    /*
    =============================================================================================
    // 커뮤니티 게시글 Admin 계정에서 삭제기능 구현
    @Transactional
    public void deletePost(Long id) {
        // 1. 필요한 경우 관련 파일이나 댓글을 먼저 삭제하는 로직을 넣을 수 있습니다.
        // 2. 게시글 삭제 실행
        postMapper.deletePost(id);
    }
    =============================================================================================

    // 현재 상태가 ACTIVE면 HIDDEN으로, 아니면 ACTIVE로 변경<POST페이지로 이동>
    @Transactional
    public void togglePostStatus(Long id, String currentStatus) {
        // 2/24 수정
        // String newStatus = "ACTIVE".equals(currentStatus) ? "BANNED" : "ACTIVE";

        // Post 도메인도 Status Enum을 쓰도록 변경
        Status newStatus = "ACTIVE".equals(currentStatus) ? Status.BANNED : Status.ACTIVE;
        postMapper.updatePostStatus(id, newStatus);
    }
    =============================================================================================
     */

    @Transactional
    public void deleteReview(Long id) {
        reviewMapper.deleteAttributeByReviewId(id); // 장단점 삭제
        reviewMapper.deleteReplyByReviewId(id);     // 댓글 삭제
        reviewMapper.deleteReactionByReviewId(id);  // 반응 삭제
        reviewMapper.deleteReviewById(id);          // 최종 리뷰 삭제
    }

    @Transactional
    public void toggleReviewStatus(Long id, String targetStatus) {
        Status statusEnum = Status.valueOf(targetStatus);
        reviewMapper.updateReviewStatus(id, statusEnum);

        if (statusEnum == Status.ACTIVE) {
            reviewMapper.updateCertificationStatus(id, Status.ACTIVE);

            ReviewDTO reviewDTO = reviewMapper.findById(id);
            if(reviewDTO != null) {
                alarmService.sendReviewAlarm(reviewDTO);
            } else {
                System.out.println("2. [실패] 해당 ID로 리뷰를 찾을 수 없습니다.");
            }
        }
    }

    @Transactional
    public Page<ReviewDTO> getPendingCertifications(Pageable pageable) {
        List<ReviewDTO> content = reviewMapper.findBannedReviews(
                pageable.getPageSize(),
                (int) pageable.getOffset()
        );

        int total = reviewMapper.countBannedReviews();

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional(readOnly = true)
    public ReviewDTO getAdminReviewDetail(Long id) {
        // 1. 리뷰 기본 정보 조회
        ReviewDTO reviewDTO = reviewMapper.findById(id);

        // 2. [NPE 방지] 데이터가 없으면 예외 발생 (컨트롤러로 가기 전에 차단)
        if (reviewDTO == null) {
            throw new RuntimeException("해당 리뷰를 찾을 수 없습니다. ID: " + id);
        }

        // 3. 좋아요 개수 설정 (기본 로직 유지)
        // reactionMapper가 주입되어 있어야 합니다. (안되어 있다면 위에 private final로 추가)
        // long likeCount = reactionMapper.countLikes("review", id);
        // reviewDTO.setLikeCount(likeCount);

        // 4. 실거주 인증 파일 경로 가져오기 (핵심!)
        // 이미 Mapper SQL에서 f.file_path AS filePath로 조인을 해두었다면 자동으로 담기지만,
        // 만약 안 나온다면 아래처럼 직접 조회해서 넣어줄 수도 있습니다.
        List<File> certFiles = fileMapper.findAllByTargetTypeAndTargetId("CERTIFICATION", id);
        if (!certFiles.isEmpty()) {
            reviewDTO.setFilePath(certFiles.get(0).getFilePath());
        }

        return reviewDTO;
    }

    @CacheEvict(value = "mainNotices", allEntries = true)
    public boolean toggleNoticeStatus(Long id, String status) {
        return noticeMapper.updateNoticeStatus(id, status) == 1;
    }

    public NoticeDTO getNoticeById(Long id) {
        return noticeMapper.findNoticeById(id);
    }

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void updateNotice(Long id, Notice notice, MultipartFile imageFile) throws IOException {
        NoticeDTO existingNotice = noticeMapper.findNoticeById(id);
        notice.setId(id);
        notice.setStatus(existingNotice.getStatus());

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingNotice.getFilePath() != null) {
                fileUtilService.deleteFileByPath(existingNotice.getFilePath());
                fileUtilService.deleteFilesByTargetTypeAndTargetId("notice", id);
            }

            String newFilePath = fileUtilService.saveFile(imageFile, "notice");
            File file = new File();
            file.setTargetType("notice");
            file.setTargetId(id);
            file.setFilePath(newFilePath);
            file.setFileSize(imageFile.getSize());

            fileUtilService.saveFileToDB(file);
        }

        noticeMapper.updateNotice(notice);
    }

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void deleteNoticeById(Long id) {
        NoticeDTO noticeDTO = noticeMapper.findNoticeById(id);
        if (noticeDTO == null) {
            throw new RuntimeException("해당 공지사항을 찾을 수 없습니다. ID: " + id);
        }

        noticeMapper.deleteNoticeById(id);
        fileUtilService.deleteFilesByTargetTypeAndTargetId("notice", id);

        if (noticeDTO.getFilePath() != null && !noticeDTO.getFilePath().isEmpty()) {
            fileUtilService.deleteFileByPath(noticeDTO.getFilePath());
        }
    }

    public int countPendingCertifications() {
        return reviewMapper.countBannedReviews();
    }

    @Transactional
    public void rejectCertification(Long reviewId, String message) {
        // 1. 리뷰 상태를 BANNED로 변경 (목록 노출 차단)
        reviewMapper.updateReviewStatus(reviewId, Status.BANNED);

        // 2. 인증 테이블의 마지막 PENDING 건을 찾아서 반려 상태와 메시지 업데이트
        // ReviewMapper.xml에 updateCertificationResult 쿼리 필요
        reviewMapper.updateCertificationResult(reviewId, Status.BANNED, message);
    }
}

