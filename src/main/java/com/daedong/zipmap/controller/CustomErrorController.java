package com.daedong.zipmap.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // 1. request에서 에러 상태 코드를 가져옵니다.
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            return switch (statusCode){
                case 403 -> "error/403";
                case 404 -> "error/404";
                case 500 -> "error/500";
                default -> "error/error";
            };
        }
        return "error/error";
    }
}
