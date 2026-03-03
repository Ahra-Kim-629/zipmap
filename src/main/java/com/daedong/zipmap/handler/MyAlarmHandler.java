package com.daedong.zipmap.handler;

import com.daedong.zipmap.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Component
public class MyAlarmHandler extends TextWebSocketHandler {
    // 현재 접속 중인 모든 사용자들의 Session 저장
    // <사용자ID, 웹소켓세션>
    private Map<String, WebSocketSession> userSessions = new HashMap<>();

//     사용자가 로그인하여 연결됐을때 실행
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Spring Security의 Authentication 객체로 형변환하여 상세 정보 꺼내기
//        Authentication authentication = (Authentication) session.getPrincipal();
//
//        // 로그인 여부 확인
//        if (authentication != null && authentication.getPrincipal() instanceof User) {
//            User user = (User) authentication.getPrincipal();
//            String userId = String.valueOf(user.getId()); // 숫자 ID를 문자열로 변환
//
//            userSessions.put(userId, session);
//            System.out.println(userId + "번 사용자 알림 서버 연결 성공");
//        }
//
//    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. Principal 객체를 통해 접속한 유저의 정보를 가져옵니다.
        Principal principal = session.getPrincipal();

        // 2. 로그인 상태인지 확인 (비로그인 유저는 principal이 null입니다)
        if (principal != null) {
            // principal.getName()은 일반 로그인/소셜 로그인 모두 고유한 아이디를 반환합니다.
            String loginId = principal.getName();

            // 3. 유저의 아이디(문자열)를 키로 사용하여 세션을 저장합니다.
            userSessions.put(loginId, session);

            System.out.println(loginId + "님 알림 서버 연결 성공"); //
        }
    }

    // 사용자가 로그아웃 했을때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Principal principal = session.getPrincipal();

        if (principal != null) {
            String userId = principal.getName();  // 로그인 한 ID
            userSessions.remove(userId, session);
            System.out.println("연결 종료: " + userId);
        }
    }

    public void sendAlarm(String userId, String message) throws Exception {
        // 명단에서 해당 사용자의 session 찾기
        WebSocketSession session = userSessions.get(userId);

        // 접속중이면 메세지 전송
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
            System.out.println(userId + "님에게 알림 전송 성공");
        } else {
            System.out.println(userId + "님이 오프라인이라 실시간 전송은 생략. (DB에만 저장)");
        }
    }
}
