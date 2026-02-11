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

    // 리뷰 파일 저장
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

    // 커뮤니티 게시판 파일 저장
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

    // 섬머노트 파일첨부를 위한 메서드
    public String saveSummernoteFile(MultipartFile file) throws IOException {
        // 1. 파일이 비었는지 확인
        if (file.isEmpty()) return null;

        // 2. 이름 생성 (기존 로직과 동일)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File saveFile = new File(uploadDir, fileName);

        // 3. 물리적 저장 (DB 저장은 여기서 하지 않음)
        file.transferTo(saveFile);

        // 4. 저장된 파일명만 리턴 (컨트롤러가 받아서 브라우저에 전달할 용도)
        return fileName;
    }

    public void deleteReviewFile(Long fileId) {
        // 1. DB에서 파일 정보 조회 (파일명을 알아야 하니까요!)
        ReviewFile file = fileMapper.getFileById(fileId);

        if (file != null) {
            // 2. 실제 물리 파일 삭제
            File realFile = new File(uploadDir, file.getFilePath());
            if (realFile.exists()) {
                realFile.delete(); // 실제 파일 삭제!
            }

            // 3. DB 데이터 삭제
            fileMapper.deleteFileById(fileId);
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
