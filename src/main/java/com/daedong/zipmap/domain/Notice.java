package com.daedong.zipmap.domain;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Notice {
    private long id;
    private String title;
    private String imagePath;
    private String linkUrl;
    private String status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;
    private int priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
