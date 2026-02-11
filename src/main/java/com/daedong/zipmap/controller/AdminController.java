package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.AdminService;
import com.daedong.zipmap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

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

    // admin/members 회원 전체 리스트 가져오기 2026.2.11 종빈 생성
    @GetMapping("/members")
    public String userList(Model model) {
        // 'UserService'가 아니라 주입받은 변수 'userService'입니다!
        List<User> userList = adminService.findAllUsers();
        model.addAttribute("userList", userList);

        return "admin/members";
    }
    // admin/members 권한 ,상태 변경 기능 추가 2026.2.11 종빈 생성
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("id") long id,
                               @RequestParam("role") String role,
                               @RequestParam("accountStatus") String status) {

        // 두 정보를 모두 포함해서 업데이트를 실행합니다.
        adminService.updateAccountStatus(id, status, role);

        return "redirect:/admin/members";
    }



}
