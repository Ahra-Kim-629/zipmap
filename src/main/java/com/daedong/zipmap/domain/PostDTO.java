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
    private Category category;
    private Location location;
    private Long likeCount;
    private Long viewCount;

    // 2/24 수정
    // private String postStatus;
    private Status postStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId; // 로그인 아이디로 커뮤니티 글에 나오게 하기 위해 추가
    private Long dislikeCount;

    private List<File> fileList;
    private List<ReplyDTO> replyList;
}
