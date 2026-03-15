package com.daedong.zipmap.domain.subscription.service;

import com.daedong.zipmap.domain.member.entity.Member;
import com.daedong.zipmap.global.security.auth.UserPrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyAlarmHandler extends TextWebSocketHandler {
    // 현재 접속 중인 모든 사용자들의 Session 저장
    // <사용자ID, 웹소켓세션>
    private Map<String, WebSocketSession> userSessions = new HashMap<>();

    // 사용자가 로그인하여 연결됐을때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Authentication auth = (Authentication) session.getPrincipal();

        if (auth != null && auth.getPrincipal() instanceof UserPrincipalDetails) {
            // UserPrincipalDetails
            UserPrincipalDetails details = (UserPrincipalDetails) auth.getPrincipal();

            // User 정보의 ID
            Member member = details.getMember();
            String userId = String.valueOf(member.getId());

            userSessions.put(userId, session);
        } else {
            System.out.println("[연결실패] 로그인 정보가 UserPrincipalDetails 타입이 아닙니다.");
        }
    }


//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // 1. Principal 객체를 통해 접속한 유저의 정보를 가져옵니다.
//        Principal principal = session.getPrincipal();
//
//        // 2. 로그인 상태인지 확인 (비로그인 유저는 principal이 null입니다)
//        if (principal != null) {
//            // principal.getName()은 일반 로그인/소셜 로그인 모두 고유한 아이디를 반환합니다.
//            String loginId = principal.getName();
//
//            // 3. 유저의 아이디(문자열)를 키로 사용하여 세션을 저장합니다.
//            userSessions.put(loginId, session);
//
//            System.out.println(loginId + "님 알림 서버 연결 성공"); //
//        }
//    }

    // 사용자가 로그아웃 했을때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 객체 자체를 찾아서 지우는것이 가장 안전
        userSessions.values().remove(session);
    }

    public void sendAlarm(String userId, String message, String url, Long alarmId) throws Exception {
        // 명단에서 해당 사용자의 session 찾기
        WebSocketSession session = userSessions.get(userId);

        // 접속중이면 메세지 전송
        if (session != null && session.isOpen()) {
            String payload = message + "|" + url + "|" + alarmId;
            session.sendMessage(new TextMessage(payload));
        } else {
            System.out.println(userId + "님이 오프라인이라 실시간 전송은 생략. (DB에만 저장)");
        }
    }
}
