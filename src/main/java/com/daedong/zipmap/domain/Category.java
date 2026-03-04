package com.daedong.zipmap.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    FREE("자유"),
    GROUP("공구"),
    SHARE("나눔"),
    TRADE("거래"),
    QNA("질문"),
    NOTICE("공지"); // 기존 코드에 있는 공지사항도 포함

    private final String displayName;

}
