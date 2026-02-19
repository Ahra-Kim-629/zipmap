package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Certification;
import com.daedong.zipmap.domain.File;
import com.daedong.zipmap.domain.Token;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.FileMapper;
import com.daedong.zipmap.mapper.UserMapper;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public User findByNameAndEmail(String name, String email) {
        return userMapper.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다."));
    }

//    public User findById(long id) {
//        return userMapper.findById(id).orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
//    }

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

    private void validateDuplicateUser(String loginId) {
        if (userMapper.findByLoginId(loginId) != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public User findByLoginId(String login_id) {
        return userMapper.findByLoginId(login_id);
    }

    @Transactional
    public void passwordReset(String loginId, String name, String email, String clientIp) throws Exception {
        User user = userMapper.findByLoginIdNameEmail(loginId, name, email);
        if (user == null) {
            throw new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다.");
        }

        Token token = new Token();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(user.getId());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiredAt(LocalDateTime.now().plusMinutes(10));
        token.setRequestedIp(clientIp);

        userMapper.insertToken(token);

        String resetLink = "http://localhost:8080/users/reset-password?token=" + token.getToken();
        mailService.sendPasswordResetMail(email, resetLink);
    }

    @Transactional
    public void confirmReset(String token, String newPassword, String usedIp) {
        Token tokenData = selectValidToken(token);
        if (tokenData == null) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        User user = userMapper.findById(tokenData.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(user);

        tokenData.setUsedYn('Y');
        tokenData.setUsedIp(usedIp);
        userMapper.updateToken(tokenData);
    }

    public Token selectValidToken(String token) {
        return userMapper.selectValidToken(token);
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