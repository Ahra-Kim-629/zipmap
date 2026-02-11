package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final NoticeMapper noticeMapper;
    private final FileService fileService;

    @Transactional
    @CacheEvict(value = "mainNotices", allEntries = true)
    public void insertNotice(Notice notice, MultipartFile imageFile) throws IOException {
        noticeMapper.insertNotice(notice);

        String fileName = fileService.saveNoticeImage(notice.getId(), imageFile);

        notice.setImagePath(fileName);
        noticeMapper.updateNoticeImagePath(notice);
    }

    @Cacheable(value = "mainNotices")
    public List<Notice> getCurrentNoticeList() {
        return noticeMapper.findCurrentNoticeList();

    }
}
