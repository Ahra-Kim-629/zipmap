package com.daedong.zipmap.domain.subscription.service;

import com.daedong.zipmap.domain.subscription.dto.AlarmDTO;
import com.daedong.zipmap.domain.post.entity.Post;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.subscription.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final MyAlarmHandler myAlarmHandler;
    private final AlarmMapper alarmMapper; // 알림 내역 저장, 알림설정자 조회

    // 커뮤니티 전용
    public void sendPostAlarm(Post post) {
        // DB에서 키워드를 구독중인 사람들의 리스트를 가져옴
        List<Map<String, Object>> subscribers = alarmMapper.selectSubscribersByPostContent(
                post.getTitle(),
                post.getContent()
        );

        // 리스트에 한명이라도 있을시 알림 전송
        for (Map<String, Object> sub : subscribers) {
            if (sub.get("userId") == null) continue;
            String subscriberId = String.valueOf(sub.get("userId"));
            String keyword = (sub.get("keyword") != null) ? String.valueOf(sub.get("keyword")) : "알림";

            String message = "[" + keyword + "] 키워드가 포함된 새 글이 올라왔습니다 : " + post.getTitle();
            AlarmDTO alarmDTO = new AlarmDTO();
            try {
                alarmDTO.setUserId(Long.parseLong(subscriberId)); // 알림 받을 사람
                alarmDTO.setTargetType("POST");
                alarmDTO.setTargetId(post.getId());               // 게시글 번호
                alarmDTO.setMessage(message);
                alarmDTO.setIsRead("N");

                alarmMapper.insertAlarm(alarmDTO); // DB에 한 줄 저장
            } catch (Exception e) {
                System.err.println("커뮤니티 알림 DB 저장 실패: " + e.getMessage());
            }

            try {
                String moveUrl = "/post/detail/" + post.getId(); // 이동할 주소
                System.out.println("알림 전송 시도 대상 ID: [" + subscriberId + "]");
                myAlarmHandler.sendAlarm(subscriberId, message, moveUrl, alarmDTO.getId());
            } catch (Exception e) {
                System.err.println("알림 전송 중 오류 발생: " + e.getMessage());
            }
        }
    }

    // 리뷰 전용
    public void sendReviewAlarm(ReviewDTO review) {

        String title = (review.getTitle() != null) ? review.getTitle() : "";
        String content = (review.getContent() != null) ? review.getContent() : "";
        String prosText = (review.getProsList() != null) ? String.join(" ", review.getProsList()) : "";
        String consText = (review.getConsList() != null) ? String.join(" ", review.getConsList()) : "";
        String address = (review.getAddress() != null) ? review.getAddress() : "";

        // DB에서 키워드를 구독중인 사람들의 리스트를 가져옴
        List<Map<String, Object>> subscribers = alarmMapper.selectSubscribersByReviewContent(
                title,
                content,
                address,
                prosText,
                consText
        );
        System.out.println("리뷰 알림 대상자 수: " + (subscribers != null ? subscribers.size() : 0));

        // 리스트에 있는 사람들에게 알림 전송
        for (Map<String, Object> sub : subscribers) {
            if (sub.get("userId") == null) continue;
            String subscriberId = String.valueOf(sub.get("userId"));
            String keyword = (sub.get("keyword") != null) ? String.valueOf(sub.get("keyword")) : "알림";

            String message = "[" + keyword + "] 키워드가 포함된 새 리뷰가 등록되었습니다: " + review.getTitle();
            AlarmDTO alarmDTO = new AlarmDTO();
            try {
                alarmDTO.setUserId(Long.parseLong(subscriberId)); // 알림 받을 사람
                alarmDTO.setTargetType("REVIEW");               // 알림 타입
                alarmDTO.setTargetId(review.getId());             // 리뷰 글 번호
                alarmDTO.setMessage(message);                     // 알림 메시지
                alarmDTO.setIsRead("N");                          // 기본값: 안읽음

                // Mapper를 통해 DB에 저장 (이게 있어야 나중에 마이페이지에서 보임!)
                alarmMapper.insertAlarm(alarmDTO);
            } catch (Exception e) {
                System.err.println("DB 알림 저장 실패: " + e.getMessage());
            }

            try {
                String moveUrl = "/review/detail/" + review.getId(); // 이동할 주소
                myAlarmHandler.sendAlarm(subscriberId, message, moveUrl, alarmDTO.getId());
            } catch (Exception e) {
                System.err.println("리뷰 알림 전송 실패: " + e.getMessage());
            }
        }
    }

    public int getUnreadCount(Long id) {
        return alarmMapper.countUnreadAlarm(id);
    }

    public List<AlarmDTO> getAlarmList(Long id) {
        return alarmMapper.selectAlarmList(id);
    }

    public void markAsRead(Long alarmId) {
        // DB에 있는 해당 알림의 is_read를 'Y'로 업데이트
        int result = alarmMapper.updateAlarmReadStatus(alarmId);

        // 결과값이 0이라면 알림 없음
        if (result == 0) {
            System.out.println("알림 읽음 처리 실패: 존재하지 않는 알림 ID - " + alarmId);
        }
    }

    @Transactional
    public void deleteById(Long id) {
        alarmMapper.deleteAlarmById(id);
    }
}
