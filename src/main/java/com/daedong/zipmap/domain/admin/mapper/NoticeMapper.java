package com.daedong.zipmap.domain.admin.mapper;

import com.daedong.zipmap.domain.admin.entity.Notice;
import com.daedong.zipmap.domain.admin.dto.NoticeDTO;
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
