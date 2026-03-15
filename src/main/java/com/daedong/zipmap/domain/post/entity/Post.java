package com.daedong.zipmap.domain.post.entity;

import com.daedong.zipmap.domain.post.enums.Category;
import com.daedong.zipmap.domain.post.enums.Location;
import com.daedong.zipmap.global.common.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private long id;
    private String title;
    private String content;
    private long userId;
    private Category category;
    private Location location;
    private long likeCount;
    private long viewCount;

    // 2/24 수정
    // private String postStatus;
    private Status postStatus;

    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
