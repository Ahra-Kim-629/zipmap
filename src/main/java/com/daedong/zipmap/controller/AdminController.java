package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Notice;
import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.AdminService;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final FileUtilService fileUtilService;
    private final PostService postService;


    @GetMapping
    public String adminMain() {
        return "/admin/main";
    }

    @GetMapping("/notice")
    public String notice() {
        return "/admin/notice-form";
    }

    @PostMapping("/notice")
    public String writeNotice(Notice notice, MultipartFile imageFile, RedirectAttributes rttr) {
        if (notice.getPriority() < 0) {
            rttr.addFlashAttribute("error", "우선순위는 0 이상의 숫자만 입력 가능합니다.");
            return "redirect:/admin/notice";
        }

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

    @GetMapping("/reviews")
    public String adminReviewList(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                  @RequestParam(required = false) String searchType,
                                  @RequestParam(required = false) String keyword,
                                  Model model) {
        // 리뷰 목록과 개수 가져오기
        Page<ReviewDTO> reviewPage = reviewService.adminFindAll(searchType, keyword, pageable);
        int totalCount = reviewService.countTotal(searchType, keyword, null, null);

        model.addAttribute("reviews", reviewPage);
        model.addAttribute("totalCount", totalCount);
        return "admin/reviews"; // templates/admin/articles.html 파일로 연결
    }

    // 2. 삭제 처리 기능 추가
    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes rttr) {
        adminService.deleteReview(id);
        rttr.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/toggle-status")
    public String toggleReviewStatus(@RequestParam("id") Long id,
                                     @RequestParam("status") String status,
                                     RedirectAttributes rttr) {
        try {
            // 서비스의 토글 로직 실행
            adminService.toggleReviewStatus(id, status);
            rttr.addFlashAttribute("message", "리뷰 상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "상태 변경 중 오류가 발생했습니다.");
        }

        // 처리가 끝나면 다시 리뷰 목록 페이지로 새로고침(리다이렉트)
        return "redirect:/admin/reviews";
    }

    @GetMapping("/postnotice")
    public String postNoticeForm() {
        // templates/admin/postnotice.html 로 이동 (파일 위치 확인하세요!)
        return "admin/postnotice";
    }

    @PostMapping("/postnotice")
    public String writePostNotice(@AuthenticationPrincipal User user, Post post, RedirectAttributes rttr) {
        try {
            // 1. 작성자를 관리자 ID로 설정
            post.setUserId(user.getId());

            // 2. 공지사항임을 표시 (isNotice 필드를 1로 설정)
            // ※ Post 도메인에 isNotice 필드가 반드시 있어야 합니다.
            post.setCategory("NOTICE");

            // ★ 2. 핵심 해결책: 비어있는 location에 기본값 넣어주기
            // DB 테이블 설정에 따라 'ALL' 또는 '서울' 등 적절한 값을 넣어주세요.
            if (post.getLocation() == null || post.getLocation().isEmpty()) {
                post.setLocation("ALL");
            }

            // 3. 게시글 저장
            Long savedId = postService.write(post);

            // 4. 써머노트 이미지 처리 (기존 로직 활용)
            if (post.getContent() != null && post.getContent().contains("src=")) {
                String newContent = fileUtilService.moveTempFilesToPermanent(post.getContent(), "POST", savedId);
                postService.updateContent(savedId, newContent);
            }

            rttr.addFlashAttribute("message", "커뮤니티 공지사항이 등록되었습니다.");
            return "redirect:/admin";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "등록 실패: " + e.getMessage());
            return "redirect:/admin/postnotice";
        }
    }

    // 리뷰 글 등록시 실거주인증 사진을 같이 보게 하기 위한 기능 추가
    @GetMapping("/reviewcertification")
    public String reviewCertificationList(Model model,
                                          @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // AdminService를 통해 BANNED 리뷰만 가져옴
        Page<ReviewDTO> reviews = adminService.getPendingCertifications(pageable);

        model.addAttribute("reviews", reviews);
        return "/admin/reviewcertification";
    }

    @GetMapping("/detail/{id}") // "/review/detail/{id}"에서 "review"를 제거
    public String adminReviewDetail(@PathVariable("id") Long id, Model model) {
        ReviewDTO reviewDTO = adminService.getAdminReviewDetail(id);
        model.addAttribute("reviewDTO", reviewDTO);
        return "admin/review_detail";
    }

}

