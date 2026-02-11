package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

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
    public String writeNotice(Notice notice, MultipartFile imageFile, RedirectAttributes rttr) {
        try {
            adminService.insertNotice(notice, imageFile);
            rttr.addFlashAttribute("message", "공지사항이 등록되었습니다.");
        } catch (IOException e) {
            rttr.addFlashAttribute("error", "공지사항 등록 중 오류가 발생했습니다.");
        }

        return "redirect:/admin";
    }

}
