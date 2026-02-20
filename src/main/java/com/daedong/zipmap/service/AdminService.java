package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.domain.File;
import java.util.List;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
    private final NoticeMapper noticeMapper;
    private final PostMapper postMapper;


    private final FileUtilService fileUtilService;
    private final FileMapper fileMapper;

//    @Transactional
//    @CacheEvict(value = "mainNotices", allEntries = true)
//    public void insertNotice(Notice notice, MultipartFile imageFile) throws IOException {
//        noticeMapper.insertNotice(notice);
//
//        String fileName = fileService.saveNoticeImage(notice.getId(), imageFile);
//
//        notice.setImagePath(fileName);
//        noticeMapper.updateNoticeImagePath(notice);
//    }

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void insertNotice(Notice notice, MultipartFile imageFile) throws IOException {

        // 1. 먼저 공지사항 글부터 저장 (그래야 ID가 생김)
        noticeMapper.insertNotice(notice);

        // 2. 파일이 있으면 -> 디스크 저장 -> 'file_attachment' 테이블에 저장
        if (imageFile != null && !imageFile.isEmpty()) {

            // (1) 실제 폴더(c:/upload/notice)에 파일 저장
            String filePath = fileUtilService.saveFile(imageFile, "notice");

            // (2) ★ [핵심] 공통 파일 테이블(file_attachment)에 정보 저장!
            com.daedong.zipmap.domain.File file = new com.daedong.zipmap.domain.File();
            file.setTargetType("NOTICE");        // 구분값
            file.setTargetId(notice.getId());    // 방금 만든 공지사항 ID
            file.setFilePath(filePath);          // notice/uuid_파일명.jpg
            file.setFileSize(imageFile.getSize());

            fileMapper.insertFile(file);
        }
    }

    // 전체 회원 리스트 가져오기 2026.02.11 종빈 수정
    @Transactional
    public List<User> findAllUsers() {
        return userMapper.findAllUsers(); // 매퍼 호출
    }

    @Transactional
    public void updateAccountStatus(long id, String status, String role) {
        User user = new User();
        user.setId(id);
        user.setAccountStatus(status);
        user.setRole(role);
        userMapper.updateUserStatusAndRole(user);
    }

    @Cacheable(value = "mainNotices")
    public List<Notice> getCurrentNoticeList() {
        return noticeMapper.findCurrentNoticeList();

    }


    public List<Post> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
        return postMapper.adminFindAll(searchType, keyword, category, location, pageable);

    }

    public int getTotalCount(String searchType, String keyword, String category, String location) {
        return postMapper.countAll(searchType, keyword, category, location);
    }

    // 커뮤니티 게시글 Admin 계정에서 삭제기능 구현

    @Transactional
    public void deletePost(Long id) {
        // 1. 필요한 경우 관련 파일이나 댓글을 먼저 삭제하는 로직을 넣을 수 있습니다.
        // 2. 게시글 삭제 실행
        postMapper.deletePost(id);
    }

    // 현재 상태가 ACTIVE면 HIDDEN으로, 아니면 ACTIVE로 변경
    @Transactional
    public void togglePostStatus(Long id, String currentStatus) {

        String newStatus = "ACTIVE".equals(currentStatus) ? "BANNED" : "ACTIVE";
        postMapper.updatePostStatus(id, newStatus);
    }

    //review 관리자 접근을 위한 방법
// 1. 기존 변수 선언 아래에 추가
    private final ReviewMapper reviewMapper;

    public int countTotal(String searchType, String keyword, List<String> pros, List<String> cons) {
        // 서비스는 단순히 매퍼에게 일을 시키는 '징검다리' 역할을 합니다.
        return reviewMapper.countTotal(searchType, keyword, pros, cons);
    }

    // 2. 클래스 맨 아래에 삭제 메서드 추가
    @Transactional
    public void deleteReview(Long id) {
        reviewMapper.deleteAttributeByReviewId(id); // 장단점 삭제
        reviewMapper.deleteReplyByReviewId(id);     // 댓글 삭제
        reviewMapper.deleteReactionByReviewId(id);  // 반응 삭제
        reviewMapper.deleteReviewById(id);          // 최종 리뷰 삭제
    }
    @Transactional
    public void toggleReviewStatus(Long id, String targetStatus) {
        // HTML에서 보낸 'BANNED' 또는 'ACTIVE'가 targetStatus로 들어옵니다.
        reviewMapper.updateReviewStatusToBanned(id, targetStatus);
    }
}
