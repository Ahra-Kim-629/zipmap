package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.handler.MyAlarmHandler;
import com.daedong.zipmap.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final MyAlarmHandler myAlarmHandler;
    private final AlarmMapper alarmMapper; // 알림 내역 저장, 알림설정자 조회

    // 커뮤니티 전용
    public void sendPostAlarm(Post post) {
        // DB에서 키워드를 구독중인 사람들의 리스트를 가져옴
        List<String> subscriberIds = alarmMapper.selectSubscribersByPostContent(
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
        System.out.println("조회된 구독자 수: " + subscriberIds.size());
        // 리스트에 한명이라도 있을시 알림 전송
        for (String subscriberId : subscriberIds) {
            String message = "구독하신 키워드가 포함된 새 글이 올라왔습니다 : " + post.getTitle();

            try {
                myAlarmHandler.sendAlarm(subscriberId, message);
            } catch (Exception e) {
                System.err.println("알림 전송 중 오류 발생: " + e.getMessage());
            }
        }
    }

    // 리뷰 전용
    public void sendReviewAlarm(ReviewDTO review) {
        // DB에서 키워드를 구독중인 사람들의 리스트를 가져옴
        List<String> subscriberIds = alarmMapper.selectSubscribersByPostContent(
                review.getTitle(),
                review.getContent(),
                review.getCreatedAt()
        );

        // 리스트에 있는 사람들에게 알림 전송
        for (String targetId : subscriberIds) {
            String message = "구독하신 키워드의 새 리뷰가 승인되었습니다: " + review.getTitle();
            try {
                myAlarmHandler.sendAlarm(targetId, message);
            } catch (Exception e) {
                System.err.println("리뷰 알림 전송 실패: " + e.getMessage());
            }
        }
    }
}
