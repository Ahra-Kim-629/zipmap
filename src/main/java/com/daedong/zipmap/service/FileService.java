package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostFile;
import com.daedong.zipmap.domain.ReviewFile;
import com.daedong.zipmap.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileMapper fileMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<ReviewFile> findFilesById(Long id) {

        return null;
    }

    public void saveReviewFile(Long id, List<MultipartFile> files) throws IOException {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    File saveFile = new File(uploadDir, fileName);
                    file.transferTo(saveFile);

                    ReviewFile reviewFile = new ReviewFile();
                    reviewFile.setReviewId(id);
                    reviewFile.setFilePath(fileName);
                    fileMapper.saveReviewFile(reviewFile);
                }
            }
        }

    }

    public List<ReviewFile> findFilesByReviewId(Long id) {

        return null;
    }

    public void saveFiles(Long id, List<MultipartFile> files) throws IOException {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    File saveFile = new File(uploadDir, fileName);
                    file.transferTo(saveFile);

                    PostFile postFile = new PostFile();
                    postFile.setPostId(id);
                    postFile.setFilePath(fileName);
                    fileMapper.insertPostFile(postFile);
                }
            }
        }
    }
}
