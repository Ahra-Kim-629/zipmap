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

    private User user;
    private List<Pros> prosList;
    private List<Cons> consList;
    private List<ReviewFile> fileList;
    private List<ReviewReply> replyList;
}
