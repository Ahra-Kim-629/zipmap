package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
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

import java.util.List;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final FileService fileService;

    // 리뷰 전체 리스트
    @GetMapping
    public String list(@PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        Page<Review> reviews = reviewService.findAll(searchType, keyword, pageable);
        model.addAttribute("reviews", reviews);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        return "review/list";
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
    public String write(ReviewDTO reviewDTO, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        reviewDTO.setUserId(user.getId());
        long savedId = reviewService.save(reviewDTO);

        // 파일저장
        if (file != null && !file.isEmpty()) {
            fileService.saveFile(savedId, file);
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
    public String edit(@PathVariable Long id, ReviewDTO reviewDTO, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        reviewDTO.setId(id);
        reviewDTO.setUserId(user.getId());
        reviewService.edit(reviewDTO);

        // 파일 처리
        if(file != null && !file.isEmpty()){
            fileService.saveFile(id, file);
        }

        return "redirect:/review/detail/" + id;
    }


}
