package com.daedong.zipmap.domain;

import lombok.Data;

@Data
public class Notice {
    private Long id;
    private String title;
    private String imagePath;
    private String linkUrl;
    private char status;
    private String startDate;
    private String endDate;
    private int posTop;
    private int posLeft;
    private int priority;
    private String createdAt;
    private String updatedAt;
}
