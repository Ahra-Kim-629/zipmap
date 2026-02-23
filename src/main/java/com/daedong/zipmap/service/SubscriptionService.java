package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.SubscriptionRequest;
import com.daedong.zipmap.mapper.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public void insertKeywords(SubscriptionRequest request) {
        Long userId = request.getUserId();
        String targetType = request.getTargetType();
        List<String> keywords = request.getKeywords();

        for (String keyword : keywords) {
            // 같은 알림 설정인지 중복 체크
            int count = subscriptionMapper.checkDuplicate(userId, keyword, targetType);
            // 중복 아닐때 DB에 저장
            if (count == 0) {
                subscriptionMapper.saveSubscription(userId, keyword, targetType);
            }
        }
    }

    public List<String> getMyKeywords(Long userId, String targetType) {
        return subscriptionMapper.findKeywordsByUserId(userId, targetType);
    }
}
