package com.daedong.zipmap.config;

import com.daedong.zipmap.handler.MyAlarmHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MyAlarmHandler myAlarmHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/alarm-ws" 라는 주소로 들어오면 웹소켓 통로를 열어줌
        registry.addHandler(myAlarmHandler, "/alarm-ws")
                .addInterceptors(new HttpSessionHandshakeInterceptor())  // 로그인 정보 옮겨줌
                .setAllowedOrigins("*");
    }

}
