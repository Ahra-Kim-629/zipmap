package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.NoticeMapper;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
    private final NoticeMapper noticeMapper;
    private final FileService fileService;

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void insertNotice(Notice notice, MultipartFile imageFile) throws IOException {
        noticeMapper.insertNotice(notice);

        String fileName = fileService.saveNoticeImage(notice.getId(), imageFile);

        notice.setImagePath(fileName);
        noticeMapper.updateNoticeImagePath(notice);
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
}
