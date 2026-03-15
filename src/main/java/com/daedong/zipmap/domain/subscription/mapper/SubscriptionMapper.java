package com.daedong.zipmap.domain.subscription.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubscriptionMapper {
    // 중복체크용 (없음 = 0, 있음 = 1이상)
    int checkDuplicate(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("targetType") String targetType);

    // 알림 설정 저장
    void saveSubscription(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("targetType") String targetType);

    List<String> findKeywordsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);

    // 알림 취소
    void deleteSubscription(Long userId, String keyword, String targetType);
}
