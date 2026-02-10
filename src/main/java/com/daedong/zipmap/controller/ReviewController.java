package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String list(@PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
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
}
