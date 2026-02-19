package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.daedong.zipmap.domain.File;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String loginId; // 로그인 아이디로 커뮤니티 글에 나오게 하기 위해 추가
    private String category;
    private String location;
    private String postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<File> fileList;
    private List<Reply> replyList;

    private int likeCount;
    private int dislikeCount;
    private int viewCount;
}
