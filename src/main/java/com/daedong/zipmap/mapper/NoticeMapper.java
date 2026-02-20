package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.NoticeDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeDTO> findCurrentNoticeList();

    void insertNotice(Notice notice);

}
