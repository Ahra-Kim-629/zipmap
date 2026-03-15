package com.daedong.zipmap.domain.member.mapper;

import com.daedong.zipmap.domain.member.entity.Member;
import com.daedong.zipmap.domain.member.entity.Token;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<Member> findByNameAndEmail(String name, String email);

    Optional<Member> findById(long id);

    Member findByLoginId(String loginId);

    void save(Member member);

    Member findByLoginIdAndNameAndEmail(String loginId, String name, String email);

    void delete(Member member);

    void update(Member member);

    // 추가: 모든 유저 리스트를 가져오는 메서드 2026.02.11 종빈 생성
    List<Member> findAllUsers();

    // 추가: 관리자 전용 수정 메서드(Status , Role 만 수정 하도록 하는 기능)
    void updateUserStatusAndRole(Member member);

    void insertToken(Token token);

    Token selectValidToken(String token);

    void updateToken(Token tokenData);

    List<Member> findAllUsersForAdmin(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int countAllUsersForAdmin();

}
