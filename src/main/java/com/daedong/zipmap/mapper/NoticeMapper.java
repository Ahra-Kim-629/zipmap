package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.NoticeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeDTO> findCurrentNoticeList();

    void insertNotice(Notice notice);

    void updateNotice(Notice notice);

    List<NoticeDTO> findNoticeAll();

    int updateNoticeStatus(Long id, String status);

    NoticeDTO findNoticeById(Long id);

    void deleteNoticeById(Long id);

    List<NoticeDTO> findNoticeAllWithPaging(@Param("pageSize") int pageSize, @Param("offset") int offset);
    int countAllNotices();
}
