package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.NoticeMapper;
import com.daedong.zipmap.mapper.PostMapper;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.domain.File;
import com.daedong.zipmap.mapper.FileMapper;

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


// 기존 코드 혹시몰라서 일단 주석처리
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
            File attachment = new File();
            attachment.setTargetType("NOTICE");        // 구분값
            attachment.setTargetId(notice.getId());    // 방금 만든 공지사항 ID
            attachment.setFilePath(filePath);          // notice/uuid_파일명.jpg
            attachment.setFileSize(imageFile.getSize());

            fileMapper.insertAttachment(attachment);
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
}
