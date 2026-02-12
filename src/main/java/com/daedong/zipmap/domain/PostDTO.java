package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String category;
    private String location;
    private int likeCount;
    private int dislikeCount;
    private String postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PostReaction> reactionList;
    private List<PostFile> fileList;
    private List<Replies> replyList;
}
