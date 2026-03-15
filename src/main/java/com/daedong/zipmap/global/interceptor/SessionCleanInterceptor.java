package com.daedong.zipmap.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionCleanInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // GET 요청이면서, 정적 리소스(css, js 등)나 로그인, 에러 페이지가 아닌 '일반적인 화면 이동'일 경우
        if ("GET".equalsIgnoreCase(method)
                && !uri.startsWith("/css")
                && !uri.startsWith("/js")
                && !uri.startsWith("/images")
                && !uri.startsWith("/files")
                && !uri.equals("/error")
                && !uri.startsWith("/alarm-ws")
                && !uri.equals("/favicon.ico")) {

            // 사용자가 명시적으로 '로그인 페이지' 자체를 요청한 경우는
            // 세션을 지우지 않고 그대로 통과시켜야 SavedRequest가 보존됩니다.
            if (uri.equals("/login")) {
                return true;
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                // 사용자가 다른 페이지로 이탈했으므로, 스프링 시큐리티가 저장해둔 강제 이동 주소를 파기함
                if (session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
                    session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
                }
                session.removeAttribute("prevPage");
            }
        }
        return true;
    }
}