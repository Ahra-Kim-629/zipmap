package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostFile;
import com.daedong.zipmap.domain.ReviewFile;
import com.daedong.zipmap.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public void saveFile(long id, MultipartFile file) {

    }

    public List<ReviewFile> findFilesByReviewId(Long id) {

        return null;
    }

    public List<PostFile> findPostFileByPostId(Long id) {
        return fileMapper.findByPostId(id);
    }

    @Transactional
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

    @Transactional
    public void deleteFilesByPostId(Long id) {
        List<PostFile> fileList = findPostFileByPostId(id);
        for (PostFile file : fileList) {
            File deleteFile = new File(uploadDir, file.getFilePath());
            if (deleteFile.exists()) {
                deleteFile.delete();
            }
        }
        fileMapper.deleteFilesByPostId(id);
    }

    public String saveNoticeImage(Long id, MultipartFile imageFile) throws IOException {
        File folder = new File(uploadDir + "/notice");
        if (!folder.exists()) folder.mkdirs(); // 폴더가 없으면 생성

        String fileName = "NOTICE_" + id + "_" + imageFile.getOriginalFilename();
        File saveFile = new File(uploadDir + "/notice", fileName);
        imageFile.transferTo(saveFile);

        return fileName;
    }
}
