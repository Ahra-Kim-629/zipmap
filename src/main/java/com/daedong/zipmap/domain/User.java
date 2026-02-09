package com.daedong.zipmap.domain;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private long id;
    private String login_id;
    private String password;
    private String name;
    private char gender;
    private String phone;
    private String email;
    private String address;
    private String role;
    private String account_status;
    private Date created_at;
    private Date updated_at;



}
