package com.daedong.zipmap.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage;

        if(exception instanceof BadCredentialsException){
            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        }else if(exception instanceof UsernameNotFoundException){
            errorMessage = "존재하지 않는 아이디입니다.";
        }else {
            errorMessage = "로그인에 실패하였습니다. 관리자에게 문의하세요.";

        }

        request.getSession().setAttribute("errorMessage", errorMessage);

        response.sendRedirect("/login");
    }
}
