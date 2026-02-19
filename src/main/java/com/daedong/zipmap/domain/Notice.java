package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Notice {
    private long id;
    private String title;
    private String imagePath;
    private String linkUrl;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
