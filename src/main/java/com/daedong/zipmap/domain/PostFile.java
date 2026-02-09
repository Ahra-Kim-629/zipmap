package com.daedong.zipmap.domain;

import lombok.Data;

@Data
public class PostFile {
    private Long id;
    private Long postId;
    private String filePath;
    private String createdAt;
    private String updatedAt;

}
