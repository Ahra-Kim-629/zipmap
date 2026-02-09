package com.daedong.zipmap.postdomain;

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
    public int postStatus;
    private String createdAt;
    private String updatedAt;

}
