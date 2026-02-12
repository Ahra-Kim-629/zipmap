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
    private String loginId; // 로그인 아이디로 커뮤니티 글에 나오게 하기 위해 추가
    private String category;
    private String location;
    private int likeCount;
    private int dislikeCount;
    private String postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PostReaction> reactionList;
    private List<PostFile> fileList;
    private List<PostReply> replyList;
}
