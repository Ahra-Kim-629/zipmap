package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.NoticeDTO;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.service.AdminService;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final AdminService adminService;
    private final ReviewService reviewService;
    private final PostService postService;

    @GetMapping("/")
    public String main(Model model) {
        List<NoticeDTO> noticeList = adminService.getCurrentNoticeList();
        List<PostDTO> postDTOList = postService.getMainpagePost();
        List<ReviewDTO> reviewDTOList = reviewService.getMainpageReview();

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("reviewDTOList", reviewDTOList);
        model.addAttribute("postDTOList", postDTOList);
        return "main";
    }
}
