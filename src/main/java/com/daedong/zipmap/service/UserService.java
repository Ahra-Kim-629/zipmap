package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Status;
import com.daedong.zipmap.domain.Token;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.domain.UserRole;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

        // ✨ 새로 추가된 부분: 회원가입 시 초기 상태와 권한을 명확하게 Enum으로 꽂아줌
        user.setAccountStatus(Status.ACTIVE); // 가입하자마자 정상 활동 상태
        user.setRole(UserRole.USER);        // 기본 권한 설정

        userMapper.save(user);
    }

    private void validateDuplicateUser(String loginId) {
        if (userMapper.findByLoginId(loginId) != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public boolean isLoginIdDuplicate(String loginId) {
        User user = userMapper.findByLoginId(loginId.trim());
        return user != null;
    }

    public User findByLoginId(String login_id) {
        return userMapper.findByLoginId(login_id);
    }

    @Transactional
    public void passwordReset(String loginId, String name, String email, String clientIp) throws Exception {
        User user = userMapper.findByLoginIdAndNameAndEmail(loginId, name, email);
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

    // 전체 회원 리스트 가져오기
    @Transactional
    public List<User> findAllUsers() {
        return userMapper.findAllUsers();
    }
    //회원 Role 기능 수정
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
}
