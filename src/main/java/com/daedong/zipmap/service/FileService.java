package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReviewFile;
import com.daedong.zipmap.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMapper fileMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void saveFile(long id, MultipartFile file) {
        fileMapper.saveFile(id, file);
    }

    public List<ReviewFile> findFilesByReviewId(Long id) {
        return fileMapper.findFilesByReviewId(id);
    }


}
