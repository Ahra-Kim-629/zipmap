package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReviewFile;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewReply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.FileService;
import com.daedong.zipmap.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final FileService fileService;

    // 리뷰 열람 (상세페이지)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewService.findById(id);
        model.addAttribute("id", id);

        // 리뷰 작성자 가져오기
        String writer = reviewService.findWriterById(id);
        model.addAttribute("writer", writer);

        // 이 리뷰의 댓글 목록 가져오기
        List<ReviewReply> replies = reviewService.findReplyById(id);
        model.addAttribute("replies", replies);

        // 이 리뷰에 첨부된 파일 목록 가져오기
        List<ReviewFile> attachedFiles = fileService.findFilesByReviewId(id);
        model.addAttribute("attachedFiles", attachedFiles);

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
    public String write(Review review, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        review.setUser_id(user.getId());
        reviewService.save(review);

        // 파일저장
        if (!file.isEmpty()) {
            fileService.saveFile(review.getId(), file);
        }
        return "redirect:/review/detail/" + review.getId();
    }

    // 리뷰 수정
    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Review review = reviewService.findById(id);
        if (!review.getUser_id().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        model.addAttribute("review", review);
        List<ReviewFile> attachedFiles = fileService.findFilesByReviewId(id);
        model.addAttribute("attachedFiles", attachedFiles);

        return "review/editForm";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Review review, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user){
        Review originalReview = reviewService.findById(id);
        if (!originalReview.getUser_id().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        review.setId(id);
        reviewService.save(review);

        if (!file.isEmpty()) {
            fileService.saveFile(id, file);
        }

        return "redirect:/review/detail/" + id;
    }


}
