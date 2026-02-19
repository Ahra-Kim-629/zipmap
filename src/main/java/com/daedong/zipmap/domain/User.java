package com.daedong.zipmap.domain;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
public class User implements UserDetails {
    private long id;
    private String loginId;
    private String password;
    private String name;
    private char gender;
    private String email;
    private String address;
    private String role;
    private String accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정이 만료되지 않았는지 여부 반환 (true : 만료되지 않음)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        //계정이 잠기지 않았는지 여부 반환 (true : 잠기지 않음)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //비밀번호가 만료되지 않았는지 여부 반환 (true : 만료되지 않음)
        return true;
    }

    @Override
    public boolean isEnabled() {
        //계정이 활성화되었는지 여부 반환 (true : 활성화됨)
        return "ACTIVE".equals(this.accountStatus);
    }
}
