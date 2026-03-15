package com.daedong.zipmap.global.security.auth;

import com.daedong.zipmap.domain.member.entity.Member;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Data
public class UserPrincipalDetails implements UserDetails, OAuth2User {
    private Member member;
    private Map<String, Object> attributes;

    // 일반 로그인
    public UserPrincipalDetails(Member member) {
        this.member = member;
    }

    // OAuth 로그인
    public UserPrincipalDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getAuthorities();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return member.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return member.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return member.isEnabled();
    }

    @Override
    public String getName() {
        return member.getName(); // 혹은 attributes.get("sub") 등
    }

    public Long getId() {
        return this.member.getId();
    }
}
