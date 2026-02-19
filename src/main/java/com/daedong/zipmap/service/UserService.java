package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Token;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * [실거주 인증] 사용자가 제출한 임대차계약서 파일을 서버에 저장하고 DB에 정보를 등록.
     * @param user 현재 로그인한 사용자 정보
     * @param file 사용자가 업로드한 MultipartFile 객체
     * @throws Exception 파일 저장 중 발생할 수 있는 오류 예외 처리
     */
//    @Transactional // 데이터베이스 입력이 포함되므로 트랜잭션 처리가 필요합니다.
//    public void registerCertification(User user, org.springframework.web.multipart.MultipartFile file) throws Exception {
//
//        // 1. 파일이 비어있는지 먼저 체크.
//        if (file == null || file.isEmpty()) {
//            throw new RuntimeException("업로드된 파일이 없습니다.");
//        }
//
//        // 2. 파일을 저장할 실제 컴퓨터 경로를 설정.
//        // 팀 프로젝트이므로 팀원들이 공통으로 쓸 수 있는 경로 혹은 본인 환경에 맞는 경로를 설정.
//        String uploadPath = "C:/zipmap_uploads/contracts/";
//
//        // 3. 해당 폴더가 컴퓨터에 없으면 자동으로 생성.
//        java.io.File folder = new java.io.File(uploadPath);
//        if (!folder.exists()) {
//            folder.mkdirs(); // 폴더 생성 명령
//        }
//
//        // 4. 파일 이름이 중복되면 덮어쓰기가 될 수 있으므로 랜덤한 ID(UUID)를 파일명 앞에 붙여줍니다.
//        String originalName = file.getOriginalFilename(); // 사용자가 올린 원래 이름 (예: 계약서.jpg)
//        String uuid = UUID.randomUUID().toString(); // 랜덤 아이디 생성
//        String storedName = uuid + "_" + originalName; // 실제 저장될 이름 (예: 12a34b..._계약서.jpg)
//
//        // 5. 설정한 경로에 파일을 실제로 저장(복사).
//        file.transferTo(new java.io.File(uploadPath + storedName));
//
//        // 6. DB(certification 테이블)에 저장하기 위해 Certification 객체를 생성하고 데이터를 채움.
//        // 주의: 이전에 만든 Certification 도메인 객체를 사용.
//        com.daedong.zipmap.domain.Certification cert = new com.daedong.zipmap.domain.Certification();
//        cert.setUserId(user.getId());          // 신청자의 고유 번호(ID)
//        cert.setOriginalName(originalName);    // 원본 파일명 기록
//        cert.setStoredPath(uploadPath + storedName); // 실제 파일이 저장된 전체 경로 기록
//
//        // 7. Mapper를 통해 DB에 최종적으로 저장.
//        userMapper.insertCertification(cert);
//
//        // 로그 출력 (콘솔에서 확인용)
//        System.out.println("✅ 실거주 인증 신청 완료: 유저 = " + user.getLoginId() + ", 파일 = " + storedName);
//    }
}
