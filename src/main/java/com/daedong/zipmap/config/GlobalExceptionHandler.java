package com.daedong.zipmap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex){
        // 1. 기존 코드: log.error("Unhandled Exception occurred: , ex");

        // 2. 수정 코드: ex를 따옴표 밖으로 빼서 진짜 에러 내용을 출력하게 합니다.
        log.error("Unhandled Exception occurred: ", ex);

        return "error/error";
    }
}