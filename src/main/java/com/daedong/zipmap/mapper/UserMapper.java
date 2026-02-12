package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Certification;
import com.daedong.zipmap.domain.Token;
import com.daedong.zipmap.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findByNameAndEmail(String name, String email);

    Optional<User> findById(long id);

    User findByLoginId(String loginId);

    void save(User user);

    User findByLoginIdNameEmail(String loginId, String name, String email);

    void delete(User user);

    void update(User user);

    // 추가: 모든 유저 리스트를 가져오는 메서드 2026.02.11 종빈 생성
    List<User> findAllUsers();

    // 추가: 관리자 전용 수정 메서드(Status , Role 만 수정 하도록 하는 기능)
    // 2026.02.11 종빈 생성
    void updateUserStatusAndRole(User user);

    void insertToken(Token token);

    Token selectValidToken(String token);

    void updateToken(Token tokenData);

    /**
     * [실거주 인증] 사용자가 올린 인증 서류 정보를 DB에 저장
     * 2026.02.12 실거주 인증 기능 추가
     */
    void insertCertification(Certification certification);
}
