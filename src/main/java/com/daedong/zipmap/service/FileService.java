package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.ReviewFile;
import org.springframework.stereotype.Service;import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileService {
    public List<ReviewFile> findFilesById(Long id) {

        return null;
    }

    public void saveFile(long id, MultipartFile file) {

    }

    public List<ReviewFile> findFilesByReviewId(Long id) {

        return null;
    }
}
