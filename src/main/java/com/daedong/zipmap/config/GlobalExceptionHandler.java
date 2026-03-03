package com.daedong.zipmap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex){
        // [수정] 예외 객체(ex)를 로그에 함께 출력하도록 수정
        log.error("Unhandled Exception occurred: ", ex);
        return "error/error";
    }
}
