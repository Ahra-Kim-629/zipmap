package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserMapper userMapper;


    @Transactional
    public void signUp(User user) {
        validateDuplicateUser(user.getLogin_id());
    }

    private void validateDuplicateUser(String login_id){
        if(userMapper.findByLoginId(login_id) != null){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public User findByLoginId(String login_id) {
        return userMapper.findByLoginId(login_id);
    }
}
