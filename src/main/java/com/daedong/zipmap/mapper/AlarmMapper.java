package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.AlarmDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AlarmMapper {

    List<String> selectSubscribersByPostContent(@Param("title") String title, @Param("content") String content);

    List<String> selectSubscribersByReviewContent(@Param("title") String title, @Param("content") String content);

    void insertAlarm(AlarmDTO alarmDTO);

    int countUnreadAlarm(Long userId);

    List<AlarmDTO> selectAlarmList(Long id);

    int updateAlarmReadStatus(Long alarmId);
}
