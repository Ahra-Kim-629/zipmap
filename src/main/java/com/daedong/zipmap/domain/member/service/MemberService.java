package com.daedong.zipmap.domain.member.service;

import com.daedong.zipmap.domain.member.entity.Member;
import com.daedong.zipmap.domain.member.entity.Token;
import com.daedong.zipmap.domain.member.enums.MemberRole;
import com.daedong.zipmap.global.common.enums.Status;
import com.daedong.zipmap.global.security.auth.UserPrincipalDetails;
import com.daedong.zipmap.domain.member.mapper.MemberMapper;
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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public Member findByNameAndEmail(String name, String email) {
        return memberMapper.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다."));
    }

    @Override
    public UserDetails loadUserByUsername(String login_id) throws UsernameNotFoundException {
        Member member = memberMapper.findByLoginId(login_id);
        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다." + login_id);
        }
        return new UserPrincipalDetails(member);
    }

    @Transactional
    public void signUp(Member member) {
        validateDuplicateUser(member.getLoginId());
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // ✨ 새로 추가된 부분: 회원가입 시 초기 상태와 권한을 명확하게 Enum으로 꽂아줌
        member.setAccountStatus(Status.ACTIVE); // 가입하자마자 정상 활동 상태
        member.setRole(MemberRole.USER);        // 기본 권한 설정

        memberMapper.save(member);
    }

    private void validateDuplicateUser(String loginId) {
        if (memberMapper.findByLoginId(loginId) != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public boolean isLoginIdDuplicate(String loginId) {
        Member member = memberMapper.findByLoginId(loginId.trim());
        return member != null;
    }

    public Member findByLoginId(String login_id) {
        return memberMapper.findByLoginId(login_id);
    }

    @Transactional
    public void passwordReset(String loginId, String name, String email, String clientIp) throws Exception {
        Member member = memberMapper.findByLoginIdAndNameAndEmail(loginId, name, email);
        if (member == null) {
            throw new RuntimeException("해당 정보로 가입된 회원을 찾을 수 없습니다.");
        }

        Token token = new Token();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(member.getId());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiredAt(LocalDateTime.now().plusMinutes(10));
        token.setRequestedIp(clientIp);

        memberMapper.insertToken(token);

        String resetLink = "http://localhost:8080/users/reset-password?token=" + token.getToken();
        mailService.sendPasswordResetMail(email, resetLink);
    }

    @Transactional
    public void confirmReset(String token, String newPassword, String usedIp) {
        Token tokenData = selectValidToken(token);
        if (tokenData == null) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        Member member = memberMapper.findById(tokenData.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

        member.setPassword(passwordEncoder.encode(newPassword));
        memberMapper.update(member);

        tokenData.setUsedYn('Y');
        tokenData.setUsedIp(usedIp);
        memberMapper.updateToken(tokenData);
    }

    public Token selectValidToken(String token) {
        return memberMapper.selectValidToken(token);
    }

    @Transactional
    public void unregister(Member member) {
        memberMapper.delete(member);
    }

    @Transactional
    public void update(Member member) {
        memberMapper.update(member);
    }

    // 전체 회원 리스트 (페이징 처리)
    @Transactional(readOnly = true)
    public Page<Member> findAllUsers(Pageable pageable) {
        // 1. 현재 페이지에 해당하는 데이터만 가져옴
        List<Member> content = memberMapper.findAllUsersForAdmin(
                pageable.getPageSize(),
                (int) pageable.getOffset()
        );

        // 2. 전체 회원이 몇 명인지 개수를 가져옴
        int total = memberMapper.countAllUsersForAdmin();

        return new PageImpl<>(content, pageable, total);
    }

    //회원 Role 기능 수정
    @Transactional
    public void updateAccountStatus(long id, String status) {
        Member member = new Member();
        member.setId(id);
        // 2/24 수정
        // user.setAccountStatus(status);
        // user.setRole(role);
        member.setAccountStatus(Status.valueOf(status));
//        user.setRole(UserRole.valueOf(role));
        memberMapper.updateUserStatusAndRole(member);
    }
}
