package com.daedong.zipmap.controller;

import com.daedong.zipmap.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 보여주기
    @GetMapping("/display")
    public ResponseEntity<Resource> displayImage(@RequestParam("fileId") String fileId) throws MalformedURLException {
        String filePath = "file:" + uploadDir + "/" + fileId;

        System.out.println("이미지를 찾는 전체 경로 >>> " + filePath);

        Resource resource = new UrlResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }
}
