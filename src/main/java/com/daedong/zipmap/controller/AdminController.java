package com.daedong.zipmap.controller;

import com.daedong.zipmap.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public String adminMain() {
        return "/admin/main";
    }

    @GetMapping("/notice")
    public String notice() {
        return "/admin/notice_form";
    }

    @PostMapping("/notice")
    public String writeNotice(String title, String content, String startDate, String endDate) {
//        public String writeNotice(Notice notice) {
//        Notice 객체를 만들어서 전달하는게 좋을 듯 함.
        adminService.insertNotice(title, content, startDate, endDate);

        return "redirect:/admin";
    }

}
