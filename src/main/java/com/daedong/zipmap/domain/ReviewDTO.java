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
    private long likeCount;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;

    private List<Reaction> reactionList;
    private List<String> prosList;
    private List<String> consList;
    private List<File> fileList;
    private List<ReplyDTO> replyList;
}
