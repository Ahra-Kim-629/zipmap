package com.daedong.zipmap.domain;

public enum Status {
    ACTIVE,  // 정상, 승인됨, 활성화됨
    PENDING, // 대기중, 심사중 (기존 SLEEP 포함)
    BANNED   // 차단됨, 숨김처리됨, 거절(DENIED)됨
}

