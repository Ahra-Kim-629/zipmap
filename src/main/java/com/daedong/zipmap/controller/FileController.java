package com.daedong.zipmap.controller;

import com.daedong.zipmap.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.MalformedURLException;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 파일 보여주기
    @GetMapping("/images/{fileId}")
    public ResponseEntity<Resource> displayImage(@PathVariable Long fileId) throws MalformedURLException {
        Resource resource = new UrlResource("file:" + uploadDir + fileId);
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().body(resource);
        }
    }

}
