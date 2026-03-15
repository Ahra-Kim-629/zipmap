package com.daedong.zipmap.global.file.service;

import com.daedong.zipmap.global.file.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUtilService {

    private final FileMapper fileMapper;

    @Value("${file.upload-dir}")
    private String uploadDir; // C:/upload

    /**
     * [1. 임시 저장]
     * 썸머노트에서 이미지를 올리면 일단 'temp' 폴더에 저장합니다.
     * (작성 취소 시 이 파일들은 그냥 버려지는 겁니다)
     */
    public String saveTempImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // 무조건 temp 폴더에 저장
        java.io.File folder = new java.io.File(uploadDir, "temp");
        if (!folder.exists()) folder.mkdirs();

        String uuid = UUID.randomUUID().toString();
        String saveFileName = uuid + "_" + file.getOriginalFilename();

        java.io.File destFile = new java.io.File(folder, saveFileName);
        file.transferTo(destFile);

        // 리턴: "temp/uuid_파일명.jpg"
        return "temp/" + saveFileName;
    }

    /**
     * [2. 파일 이사 (Temp -> Real)]
     * 글 작성이 완료되면, 본문에 있는 'temp' 경로의 파일들을
     * 진짜 폴더(review, post 등)로 옮기고, HTML 본문의 경로도 바꿔줍니다.
     * * @return 변경된 HTML 본문 (경로가 temp -> review로 바뀐 내용)
     */
    @Transactional
    public String moveTempFilesToPermanent(String content, String targetType, Long targetId) {
        if (content == null || content.isEmpty()) return "";

        String targetFolder = targetType.toLowerCase(); // 예: review
        java.io.File realFolder = new java.io.File(uploadDir, targetFolder);
        if (!realFolder.exists()) realFolder.mkdirs();

        // 1. 본문에서 "temp/..." 로 되어있는 이미지 찾기
        String regex = "src=\"/files/temp/([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        StringBuffer newContent = new StringBuffer();

        while (matcher.find()) {
            String fileName = matcher.group(1); // uuid_강아지.jpg

            // (1) 파일 이동: C:/upload/temp/파일 -> C:/upload/review/파일
            java.io.File tempFile = new java.io.File(uploadDir + "/temp", fileName);
            java.io.File destFile = new java.io.File(realFolder, fileName);

            if (tempFile.exists()) {
                if (tempFile.renameTo(destFile)) {
                    // 이동 성공 시 DB 저장
                    com.daedong.zipmap.global.file.entity.File file = new com.daedong.zipmap.global.file.entity.File();
                    file.setTargetType(targetType);
                    file.setTargetId(targetId);
                    file.setFilePath(targetFolder + "/" + fileName); // review/파일명
                    file.setFileSize(destFile.length());

                    fileMapper.insertFile(file);
                }
            }

            // (2) HTML 태그 내용 변경: /files/temp/... -> /files/review/...
            matcher.appendReplacement(newContent, "src=\"/files/" + targetFolder + "/" + fileName + "\"");
        }
        matcher.appendTail(newContent);

        return newContent.toString(); // 경로가 수정된 HTML 리턴
    }

    /**
     * [수정 시 이미지 동기화]
     * 수정할 때는 'temp'에서 오는 파일도 있고, 원래 'review'에 있던 파일도 있습니다.
     * 복합적으로 처리합니다.
     */
    @Transactional
    public String updateImagesFromContent(String content, String targetType, Long targetId) {
        // 1. 먼저 temp에 있는 파일들을 정식 폴더로 이사시킴 (신규 추가된 이미지들)
        // content 내용이 바뀝니다. (temp -> review 경로로 변경됨)
        String updatedContent = moveTempFilesToPermanent(content, targetType, targetId);

        // 2. 이제 본문(updatedContent)은 모두 "review/..." 경로를 가집니다.
        // 기존 삭제 로직 수행 (아까 만든 로직 그대로)
        Set<String> htmlPaths = new HashSet<>();
        String regex = "src=\"/files/([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(updatedContent);
        while (matcher.find()) {
            htmlPaths.add(matcher.group(1));
        }

        List<com.daedong.zipmap.global.file.entity.File> dbFiles = fileMapper.findAllByTargetTypeAndTargetId(targetType, targetId);

        // 본문에 없는 DB 파일 삭제
        for (com.daedong.zipmap.global.file.entity.File dbFile : dbFiles) {
            if (!htmlPaths.contains(dbFile.getFilePath())) {
                deleteFileByPath(dbFile.getFilePath());
                fileMapper.deleteFileById(dbFile.getId());
            }
        }

        return updatedContent;
    }

    public List<com.daedong.zipmap.global.file.entity.File> getFileList(String targetType, Long targetId) {

        return fileMapper.findFileListByTargetTypeAndTargetId(targetType, targetId);
    }

    // (단순 삭제 메서드는 그대로 유지)
    public void deleteFileByPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        java.io.File file = new java.io.File(uploadDir, filePath);
        if (file.exists()) file.delete();
    }

    // (글 삭제 시 전체 삭제 메서드 유지)
    @Transactional
    public void deleteFilesByTargetTypeAndTargetId(String targetType, Long targetId) {

        fileMapper.deleteFilesAllByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * [단일 파일 즉시 저장]
     * 썸머노트 같은 거 안 쓰고, 그냥 첨부파일 1개를 바로 특정 폴더에 저장할 때 씁니다.
     * 예: 공지사항 팝업 이미지, 프로필 사진 등
     */
    public String saveFile(MultipartFile file, String targetFolder) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 1. 폴더 만들기 (예: c:/upload/notice)
        File folder = new File(uploadDir, targetFolder);
        if (!folder.exists()) folder.mkdirs();

        // 2. 파일명 중복 방지 (UUID)
        String uuid = UUID.randomUUID().toString();
        String saveFileName = uuid + "_" + file.getOriginalFilename();

        // 3. 저장
        File destFile = new File(folder, saveFileName);
        file.transferTo(destFile);

        // 4. 경로 리턴 (예: notice/uuid_파일명.jpg)
        return targetFolder + "/" + saveFileName;
    }

    // ★ 추가: DB에 파일 정보 저장 (PostController에서 호출)
    @Transactional
    public void saveFileToDB(com.daedong.zipmap.global.file.entity.File file) {
        fileMapper.insertFile(file);
    }

}