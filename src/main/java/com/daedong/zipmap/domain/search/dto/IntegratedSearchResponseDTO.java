package com.daedong.zipmap.domain.search.dto;

import com.daedong.zipmap.domain.post.dto.PostDTO;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
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
