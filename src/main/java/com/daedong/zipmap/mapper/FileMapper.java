package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.ReviewFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileMapper {
    void saveFile(long id, MultipartFile file);

    List<ReviewFile> findFilesByReviewId(Long id);

    List<ReviewFile> findByReviewId(Long id);
}
