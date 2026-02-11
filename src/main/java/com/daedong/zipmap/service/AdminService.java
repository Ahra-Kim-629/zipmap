package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
//    private NoticeMapper noticeMapper;

    public void insertNotice(String title, String content, String startDate, String endDate) {
//        public void insertNotice(Notice notice) {
//        noticeMapper.insertNotice(title, content, startDate, endDate);
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
}
