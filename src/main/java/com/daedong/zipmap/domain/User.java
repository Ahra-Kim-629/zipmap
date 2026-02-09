package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private long id;
    private String loginId;
    private String password;
    private String name;
    private char gender;
    private String phone;
    private String email;
    private String address;
    private String role;
    private String accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
