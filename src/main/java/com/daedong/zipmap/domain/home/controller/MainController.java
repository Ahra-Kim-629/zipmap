package com.daedong.zipmap.domain.home.controller;

import com.daedong.zipmap.domain.admin.dto.NoticeDTO;
import com.daedong.zipmap.domain.post.dto.PostDTO;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.admin.service.AdminService;
import com.daedong.zipmap.domain.post.service.PostService;
import com.daedong.zipmap.domain.review.service.ReviewService;
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
    public String frontpage(Model model) {
        List<NoticeDTO> noticeDTOList = adminService.getCurrentNoticeList();
        List<PostDTO> postDTOList = postService.getMainpagePost();
        List<ReviewDTO> reviewDTOList = reviewService.getMainpageReview();

        model.addAttribute("noticeDTOList", noticeDTOList);
        model.addAttribute("reviewDTOList", reviewDTOList);
        model.addAttribute("postDTOList", postDTOList);
        return "main";
    }
}
