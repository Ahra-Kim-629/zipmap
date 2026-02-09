package com.daedong.zipmap.domain;

import lombok.Data;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    public Long userId;
    private String category;
    private String location;
    public int likeConunt;
    public int dislikeCount;
    public String postStatus;
    private String createdAt;
    private String updatedAt;

}
