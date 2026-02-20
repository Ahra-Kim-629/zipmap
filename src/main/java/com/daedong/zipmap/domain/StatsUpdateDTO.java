package com.daedong.zipmap.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsUpdateDTO {
    private Long id;
    private Long likeCount;
    private Long viewCount;
}
