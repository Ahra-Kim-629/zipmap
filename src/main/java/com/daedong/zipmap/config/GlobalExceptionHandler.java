package com.daedong.zipmap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex) throws Exception{
        if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            throw ex;
        }
        // [중요] 개발자가 원인을 파악할 수 있도록 전체 StackTrace를 로그로 남깁니다.
        log.error("[Server Error] 예상치 못한 예외 발생: ", ex);
        return "error/500";
    }
}
