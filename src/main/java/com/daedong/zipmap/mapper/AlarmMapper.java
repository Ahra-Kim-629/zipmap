package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.AlarmDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AlarmMapper {

    List<Map<String, Object>> selectSubscribersByPostContent(@Param("title") String title, @Param("content") String content);

    List<Map<String, Object>> selectSubscribersByReviewContent(@Param("title") String title, @Param("content") String content, @Param("address") String address, @Param("pros") String pros, @Param("cons") String cons);

    void insertAlarm(AlarmDTO alarmDTO);

    int countUnreadAlarm(Long userId);

    List<AlarmDTO> selectAlarmList(Long id);

    int updateAlarmReadStatus(Long alarmId);

    void deleteAlarmById(Long id);
}
