package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public User findId(String name, String email) {
        return userMapper.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다."));
    }

    public User findById(long id) {
        return userMapper.findById(id).orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
    }


    @Override
    public UserDetails loadUserByUsername(String login_id) throws UsernameNotFoundException {
        User user = userMapper.findByLoginId(login_id);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다." + login_id);
        }
        return user;
    }

    @Transactional
    public void signUp(User user) {
        validateDuplicateUser(user.getLoginId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.save(user);
    }

    private void validateDuplicateUser(String login_id) {
        if (userMapper.findByLoginId(login_id) != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }


    public User findByLoginId(String login_id) {
        return userMapper.findByLoginId(login_id);
    }

    public void passwordReset(String loginId, String name, String email) throws Exception {
        User user = userMapper.findByLoginIdNameEmail(loginId, name, email);
        if (user == null) {
            throw new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다.");
        }

        String token = UUID.randomUUID().toString();
//        tokenService.saveToken(user.getId(), token);

        String resetLink = "http://localhost:8080/users/reset-password?token=" + token;
        mailService.sendPasswordResetMail(email, resetLink);
    }

    @Transactional
    public void unregister(User user) {
        userMapper.delete(user);
    }

    @Transactional
    public void update(User user) {
        userMapper.update(user);
    }
}
