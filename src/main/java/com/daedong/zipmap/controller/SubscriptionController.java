package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.SubscriptionRequest;
import com.daedong.zipmap.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    // =====================================================================
    // 알림 설정
    // =====================================================================
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveSubscription(@RequestBody SubscriptionRequest request) {
        subscriptionService.insertKeywords(request);
        return ResponseEntity.ok("알림 설정이 완료되었습니다.");
    }

    // =====================================================================
    // 알림 취소
    // =====================================================================
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteSubscription(@RequestBody SubscriptionRequest request) {
        subscriptionService.deleteKeywords(request);
        return ResponseEntity.ok("알림이 취소되었습니다.");
    }

}
