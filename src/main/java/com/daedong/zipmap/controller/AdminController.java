package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.AdminService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping
    public String adminMain() {
        return "/admin/main";
    }

    @GetMapping("/notice")
    public String notice() {
        return "/admin/notice_form";
    }

    @PostMapping("/notice")
    public String writeNotice(Notice notice,@RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes rttr) {
        try {
            adminService.insertNotice(notice, imageFile);
            rttr.addFlashAttribute("message", "공지사항이 등록되었습니다.");
        } catch (IOException e) {
            rttr.addFlashAttribute("error", "공지사항 등록 중 오류가 발생했습니다.");
        }

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

    @GetMapping("/posts")
    public String list(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String location,
                       Model model) {
        // 전체 게시판 게시글 리스트
        List<Post> posts = adminService.findAll(searchType, keyword, category, location, pageable);
        int totalCount = adminService.getTotalCount(searchType, keyword, category, location);

        model.addAttribute("posts", posts); // 게시글 리스트 (List<Post>)
        model.addAttribute("totalCount", totalCount); // 전체 글 수
        model.addAttribute("size", pageable.getPageSize()); // 한 페이지당 개수
        model.addAttribute("page", pageable.getPageNumber()); // 현재 페이지 번호

        // 검색 조건 유지
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("location", location);

        return "admin/posts";
    }

    // 커뮤니티 게시글 Admin 계정에서 삭제기능 구현

    @GetMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, RedirectAttributes rttr) {
        try {
            adminService.deletePost(id);
            rttr.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/posts";
    }

    // AdminController.java 에 추가
    @PostMapping("/posts/toggle-status")
    public String toggleStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes rttr) {
        adminService.togglePostStatus(id, status);
        rttr.addFlashAttribute("message", "게시글 상태가 성공적으로 변경되었습니다.");
        return "redirect:/admin/posts";
    }
}
