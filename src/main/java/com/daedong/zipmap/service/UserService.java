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


    private final FileUtilService fileUtilService;
    private final FileMapper fileMapper;

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

    /**
     * [실거주 인증] 사용자가 제출한 임대차계약서 파일을 서버에 저장하고 DB에 정보를 등록.
     * @param user 현재 로그인한 사용자 정보
     * @param file 사용자가 업로드한 MultipartFile 객체
     * @throws Exception 파일 저장 중 발생할 수 있는 오류 예외 처리
     */
    @Transactional
    public void registerCertification(User user, MultipartFile file) throws IOException {

        // 1. 파일 유효성 검사
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다.");
        }

        // 2. 인증 정보(글) 먼저 저장 -> ID 생성됨
        Certification cert = new Certification();
        cert.setUserId(user.getId());
        cert.setStatus("PENDING"); // 대기 상태
        // (참고: originalName, storedPath는 이제 file_attachment 테이블로 가므로 여기선 굳이 안 넣어도 됩니다.
        //  하지만 기존 DB 구조상 필요하다면 아래에서 넣습니다.)

        userMapper.insertCertification(cert); // DB 저장 (ID 생성)


        // 3. ★ 파일 저장 (FileUtilService 이용)
        // "CERTIFICATION" 폴더에 저장 (c:/upload/certification/...)
        String filePath = fileUtilService.saveFile(file, "certification");


        // 4. ★ 공통 파일 테이블(file_attachment)에 저장
        File attachment = new File();
        attachment.setTargetType("CERTIFICATION");
        attachment.setTargetId(cert.getId()); // 방금 만든 인증 ID
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());

        fileMapper.insertAttachment(attachment);
    }
}