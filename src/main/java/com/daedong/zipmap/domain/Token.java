package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Token {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private char usedYn;
    private String requestedIp;
    private String usedIp;
}
