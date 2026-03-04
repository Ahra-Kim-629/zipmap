package com.daedong.zipmap.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        // 1. 우선순위 1: 직접 파라미터로 넘어온 값 (일반 로그인 폼 제출 시)
        String prevPage = request.getParameter("prevPage");

        // 2. 우선순위 2: 세션에 수동으로 저장한 값 (로그인 컨트롤러에서 넣은 것 - OAuth2 대비)
        if (prevPage == null || prevPage.isEmpty()) {
            prevPage = (String) session.getAttribute("prevPage");
        }

        // 3. 우선순위 3: 스프링 시큐리티가 가로챈 '원래 가려던 주소' (SavedRequest)
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // --- 리다이렉트 결정 로직 ---
        if (prevPage != null && !prevPage.isEmpty()) {
            // 직접 지정한 페이지가 있으면 최우선 이동
            session.removeAttribute("prevPage"); // 세션 청소
            response.sendRedirect(prevPage);
        } else if (savedRequest != null) {
            // 시큐리티가 기억하는 주소가 있으면 이동
            response.sendRedirect(savedRequest.getRedirectUrl());
        } else {
            // 아무 정보도 없으면 메인으로
            response.sendRedirect("/");
        }
    }
}