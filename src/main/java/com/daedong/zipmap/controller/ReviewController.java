package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.FileService;
import com.daedong.zipmap.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final FileService fileService;

    @GetMapping
    public String list(@PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) List<String> pros,
                       @RequestParam(required = false) List<String> cons,
                       Model model) {

        // 페이징 리뷰
        Page<ReviewDTO> reviews = reviewService.findAll(searchType, keyword, pros, cons, pageable);

        // 지도 표시용 전체 리뷰
        List<ReviewDTO> allReviews = reviewService.findAll(searchType, keyword, pros, cons);

        // 장점/단점 체크박스 항목
        List<String> prosList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");
        List<String> consList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");

        model.addAttribute("reviews", reviews);
        model.addAttribute("allReviews", allReviews);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pros", pros);
        model.addAttribute("cons", cons);
        model.addAttribute("prosList", prosList);
        model.addAttribute("consList", consList);

        return "review/list"; // templates/review/list.html
    }

    // 리뷰 열람 (상세페이지)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ReviewDTO reviewDTO = reviewService.findById(id);
        model.addAttribute("reviewDTO", reviewDTO);

        return "review/detail";
    }

    // 리뷰 작성
    @GetMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write() {
        return "review/writeForm";
    }

    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write(ReviewDTO reviewDTO, @RequestParam("files") List<MultipartFile> files, @AuthenticationPrincipal User user) throws IOException {
        reviewDTO.setUserId(user.getId());
        Long savedId = reviewService.save(reviewDTO);

        // 파일저장
        if (files != null && !files.isEmpty()) {
            fileService.saveReviewFile(savedId, files);
        }

        return "redirect:/review/detail/" + savedId;
    }

    // 리뷰 수정
    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        ReviewDTO reviewDTO = reviewService.findById(id);
        if (reviewDTO.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        model.addAttribute("reviewDTO", reviewDTO);
        return "review/editForm";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, ReviewDTO reviewDTO, @RequestParam(value = "files", required = false) List<MultipartFile> files, @AuthenticationPrincipal User user) throws IOException {
        ReviewDTO original = reviewService.findById(id);
        if (original.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        reviewDTO.setId(id);
        reviewDTO.setUserId(user.getId());
        reviewService.edit(reviewDTO);


        // 파일 처리
        if (files != null && !files.isEmpty()) {
            fileService.saveReviewFile(id, files);
        }

        return "redirect:/review/detail/" + id;
    }

    // 섬머노트 에디터에서 이미지를 올릴때 호출

    @PostMapping("/uploadSummernoteImage")
    @ResponseBody // JSON이나 문자열로 데이터를 보낼 때 사용
    public Map<String, String> uploadSummernoteImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            // 1. 아까 만든 서비스 메서드를 사용하여 파일 저장 (파일명 반환)
            // 서비스 메서드 이름 saveSummernoteFile
            String fileName = fileService.saveSummernoteFile(file);

            // 2. 브라우저가 접근할 수 있는 경로를 응답으로 보냄
            // 예: /upload/uuid_name.jpg
            response.put("url", "/upload/" + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response; // 자바스크립트의 success: function(response)로 전달
    }

    // 섬머노트 에디터에서 수정중 파일 삭제할때 호출
    @PostMapping("/deleteFile/{fileId}")
    @ResponseBody
    public String deleteFile(@PathVariable("fileId") Long fileId) {
        try {
            // 서비스에게 삭제 명령 (DB 데이터 삭제 + 실제 파일 삭제)
            fileService.deleteReviewFile(fileId);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }
}
