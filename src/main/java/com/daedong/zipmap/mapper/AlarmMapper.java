package com.daedong.zipmap.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AlarmMapper {

    List<String> selectSubscribersByPostContent(@Param("title") String title, @Param("content") String content);
}
