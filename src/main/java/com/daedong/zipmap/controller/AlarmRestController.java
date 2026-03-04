package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.service.AlarmService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmRestController {

    private final AlarmService alarmService;

    @PostMapping("/read/{alarmId}")
    public ResponseEntity<String> readAlarm(@PathVariable Long alarmId) {
        try {
            // DB의 is_read를 'Y'로 업데이트하는 서비스
            alarmService.markAsRead(alarmId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    @GetMapping("/unread-count")
    public int getUnreadCount(@AuthenticationPrincipal UserPrincipalDetails user) {
        if (user == null) {
            System.out.println("로그인 유저를 찾을 수 없습니다.");
            return 0;
        }

        System.out.println("로그인 유저 ID: " + user.getUser().getId());
        return alarmService.getUnreadCount(user.getUser().getId());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAlarm(@PathVariable Long id) {
        alarmService.deleteById(id); // DB에서 삭제하는 로직
        return ResponseEntity.ok("success");
    }
}
