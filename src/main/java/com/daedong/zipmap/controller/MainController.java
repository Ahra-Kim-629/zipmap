package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final AdminService adminService;

    @GetMapping("/")
    public String main(Model model) {
        List<Notice> noticeList = adminService.getCurrentNoticeList();
        model.addAttribute("noticeList", noticeList);

        return "index";
    }


}
