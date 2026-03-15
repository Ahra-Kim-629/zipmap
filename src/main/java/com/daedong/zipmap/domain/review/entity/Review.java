package com.daedong.zipmap.domain.review.entity;

import com.daedong.zipmap.global.common.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private long id;
    private String title;
    private String content;
    private String address;
    private long userId;
    private int point;

    // 2/24 수정
    // private String reviewStatus;
    private Status reviewStatus;

    private long likeCount;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 2/26 추가: 화면에서 전달받을 대표 사진(썸네일) 임시 경로
    private String thumbnailPath;
}
