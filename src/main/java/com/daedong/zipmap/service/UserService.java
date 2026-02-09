package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public void signUp(User user) {
        // Implementation for sign up if needed
    }

    public User findId(String name, String email) {
        return userMapper.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다."));
    }

    public User findPassword(String name, String email) {
//        계정이 없으면 없다는 메세지를 반송하고
//        계정이 있으면 입력한 이메일로 비밀번호를 재설정할 수 있는 링크가 담긴 메일을 발송하기
//        공부할 내용이 있어서 .... 시간 두고 하겠습니다.
//        내용 : JavaMailSender
        return null;
    }

    public User findById(long id) {
        return userMapper.findById(id).orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
    }
}
