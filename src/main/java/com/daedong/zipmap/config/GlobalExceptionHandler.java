package com.daedong.zipmap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex){
        //서버 콘솔에 스택 트레이스를 남겨 개발자가 추적할 수 있도록 하기 위함
        log.error("Unhandled Exception occurred: , ex");
        return "error/error";
    }
}
