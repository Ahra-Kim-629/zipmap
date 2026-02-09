package com.daedong.zipmap.domain;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String role;
    private String account_status;
    private String created_at;
    private String updated_at;

}
