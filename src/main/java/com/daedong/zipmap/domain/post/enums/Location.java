package com.daedong.zipmap.domain.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Location {
    ALL("전체 지역"),
    SEOUL_CENTER("서울 중앙"),
    SEOUL_EAST("서울 동부"),
    SEOUL_SOUTH("서울 남부"),
    SEOUL_WEST("서울 서부"),
    SEOUL_NORTH("서울 북부");

    private final String displayName;
}
