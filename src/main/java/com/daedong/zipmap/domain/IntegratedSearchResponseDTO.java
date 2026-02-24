package com.daedong.zipmap.domain;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IntegratedSearchResponseDTO {

    private List<ReviewDTO> reviews;
    private List<PostDTO> posts;
    private String keyword;
}
