package com.daedong.zipmap.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReviewDTO {
    private long id;
    private String title;
    private String content;
    private String address;
    private long userId;
    private int point;

    // 2/24 수정
    // private String reviewStatus;
    private Status reviewStatus;

    private long likeCount;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String loginId;

    private List<Reaction> reactionList;
    private List<String> prosList;
    private List<String> consList;
    private List<File> fileList;
    private List<ReplyDTO> replyList;

    private List<String> topCrimes; // TOP 3 범죄 종류
    private List<Integer> crimeCounts; // TOP 3 범죄 건수
    private String safetyWarning; // "이 구는 서울 내 절도 1위 지역입니다" 같은 안내 문구
    private List<Map<String, Object>> topCrimeList;
    private String filePath;

    private String prosStr;

    // 2/26 추가: 화면에서 전달받을 대표 사진(썸네일) 임시 경로
    private String thumbnailPath;

    // 추가 : message 반려 사유 값 가져오기
    private String message;
}
