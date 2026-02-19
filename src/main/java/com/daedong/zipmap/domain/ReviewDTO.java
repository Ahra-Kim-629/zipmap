package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDTO {
    private long id;
    private String title;
    private String content;
    private String address;
    private long userId;
    private int point;
    private String reviewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;

    private List<String> prosList;
    private List<String> consList;
    private List<ReviewFile> fileList;
    private List<Reply> replyList;

    private int likeCount;
    private int dislikeCount;

    private String pros;
    private String cons;
}
