package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<Notice> findCurrentNoticeList();

    void insertNotice(Notice notice);

}
