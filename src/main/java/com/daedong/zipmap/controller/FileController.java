package com.daedong.zipmap.controller;

import com.daedong.zipmap.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

//    @Value("${file.upload-dir}")
//    private String uploadDir;
//
//    // 파일 보여주기
//    @GetMapping("/images/{fileId}")
//    public ResponseEntity<Resource> displayImage(@PathVariable Long fileId) throws MalformedURLException {
//        Resource resource = new UrlResource("file:" + uploadDir + fileId);
//        if (!resource.exists()) {
//            return ResponseEntity.notFound().build();
//        } else {
//            return ResponseEntity.ok().body(resource);
//        }
//    }

}
