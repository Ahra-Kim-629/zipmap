package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.*;
import com.daedong.zipmap.util.FileUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final AdminService adminService;
    private final ReviewService reviewService;
    private final FileUtilService fileUtilService;
    private final PostService postService;
    private final ReportService reportService;

    /*
     ====================================================================================================================
    ADMIN 메인 페이지
     ====================================================================================================================
     */

    @GetMapping
    public String adminMain() {
        return "/admin/main";
    }

    /*
     ====================================================================================================================
    NOTICE(베너 광고) 관리 기능
     ====================================================================================================================
     */

    @GetMapping("/notice/list")
    public String noticeList(Model model) {
        // 전체 게시판 게시글 리스트
        List<NoticeDTO> noticeDTOList = adminService.getNoticeAll();

        model.addAttribute("notices", noticeDTOList);

        return "admin/notice/list";
    }


    @GetMapping("/notice/write")
    public String notice() {
        return "/admin/notice/write-form";
    }

    @PostMapping("/notice/write")
    public String writeNotice(Notice notice, MultipartFile imageFile, RedirectAttributes rttr) {
        if (notice.getPriority() < 0) {
            rttr.addFlashAttribute("error", "우선순위는 0 이상의 숫자만 입력 가능합니다.");
            return "redirect:/admin/notice/list";
        }

        try {
            adminService.insertNotice(notice, imageFile);
            rttr.addFlashAttribute("message", "공지사항이 등록되었습니다.");
        } catch (IOException e) {
            rttr.addFlashAttribute("error", "공지사항 등록 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/notice/list";
    }

    @PostMapping("/notice/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> toggleNoticeStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {

        boolean result = adminService.toggleNoticeStatus(id, status);

        Map<String, Boolean> response = new HashMap<>();
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notice/edit/{id}")
    public String editNotice(@PathVariable("id") Long id,
                             Model model) {
        NoticeDTO noticeDTO = adminService.getNoticeById(id);
        model.addAttribute("notice", noticeDTO);
        return "/admin/notice/edit-form";
    }

    @PostMapping("/notice/edit/{id}")
    public String editNotice(@PathVariable("id") Long id, Notice notice,
                             MultipartFile imageFile, RedirectAttributes rttr) {

        if (notice.getPriority() < 0) {
            rttr.addFlashAttribute("error", "우선순위는 0 이상의 숫자만 입력 가능합니다.");
            return "redirect:/admin/notice/list";
        }

        try {
            adminService.updateNotice(id, notice, imageFile);
            rttr.addFlashAttribute("message", "공지사항이 수정되었습니다.");
        } catch (IOException e) {
            rttr.addFlashAttribute("error", "공지사항 수정 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/notice/list";
    }

    @PostMapping("/notice/delete/{id}")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes rttr) {
        try {
            adminService.deleteNoticeById(id);
            rttr.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        } catch (RuntimeException e) {
            rttr.addFlashAttribute("error", "공지사항 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/notice/list";
    }

    /*
    ====================================================================================================================
    USER 관리 기능 ( Admin Controller -> UserService 로 구현 되도록 재설정 )
    ====================================================================================================================
     */
    // admin/members 회원 전체 리스트 가져오기
    @GetMapping("/members")
    public String userList(Model model) {
        // 'UserService'가 아니라 주입받은 변수 'userService'입니다!
        List<User> userList = userService.findAllUsers(); // USERservice에서 하는 것이 관심사 분리를 하는게 좋을 거 같아서 이동
        model.addAttribute("userList", userList);

        return "admin/members";
    }

    // admin/members 권한 ,상태 변경 기능 추가 2026.2.11 종빈 생성
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("id") long id,
//                               @RequestParam("role") String role,
                               @RequestParam("accountStatus") String status) {

        // 두 정보를 모두 포함해서 업데이트를 실행합니다.
        userService.updateAccountStatus(id, status);

        return "redirect:/admin/members";
    }

    /*
    ====================================================================================================================
    POST 관련 부분
    ====================================================================================================================
     */
    // 이 부분은 POSTSERVICE 랑 겹치다 보니 POSTSERVICE 로 이동 예정
    @GetMapping("/posts")
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String location,
                       Model model) {
        // 전체 게시판 게시글 리스트
        Page<Post> postPage = postService.findAllAdmin(searchType, keyword, category, location, pageable);

        model.addAttribute("posts", postPage.getContent()); // 게시글 리스트 (List<Post>)
//        model.addAttribute("postPage", postPage); // Page 객체 통째로 추가
        model.addAttribute("totalCount", postPage.getTotalElements()); // 전체 글 수
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
    // 이 부분은 POSTSERVICE 랑 겹치다 보니 POSTSERVICE 로 이동 예정
    @GetMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, RedirectAttributes rttr) {
        try {
            postService.delete(id); // adminService.deletePost(id); 를 postService.delete(id); 로 변경
            rttr.addFlashAttribute("message", "게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/posts";
    }

    // AdminController.java 에 추가
    // 게시글 상태 토글 기능 ( 상태가 공개인지 비공개인지 ? )
    @PostMapping("/posts/toggle-status")
    public String toggleStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes rttr) {
        postService.togglePostStatus(id, status); //adminService.togglePostStatus(id, status); 를 변경
        rttr.addFlashAttribute("message", "게시글 상태가 성공적으로 변경되었습니다.");
        return "redirect:/admin/posts";
    }


    // POSTNOTICE : 게시글에 공지사항 등록 기능
    // 이 부분은 POSTSERVICE 랑 겹치다 보니 POSTSERVICE 로 이동 (고민중)
    @GetMapping("/postnotice")
    public String postNoticeForm() {
        // templates/admin/postnotice.html 로 이동 (파일 위치 확인하세요!)
        return "admin/postnotice";
    }

    // POSTNOTICE : 게시글에 공지사항 등록 기능 ( 이미 POSTSERVICE로 이동을 함 )
    @PostMapping("/postnotice")
    public String writePostNotice(@AuthenticationPrincipal UserPrincipalDetails user, Post post, RedirectAttributes rttr) {
        try {
            // 1. 작성자를 관리자 ID로 설정
            post.setUserId(user.getUser().getId());

            // 2. 공지사항임을 표시 (isNotice 필드를 1로 설정)
            // ※ Post 도메인에 isNotice 필드가 반드시 있어야 합니다.
            post.setCategory(Category.NOTICE);

            // ★ 2. 핵심 해결책: 비어있는 location에 기본값 넣어주기
            // DB 테이블 설정에 따라 'ALL' 또는 '서울' 등 적절한 값을 넣어주세요.
            if (post.getLocation() == null) {
                post.setLocation(Location.ALL);
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

    /*
    ====================================================================================================================
    REVIEW(리뷰) 관리 기능
    ====================================================================================================================
     */

    @GetMapping("/reviews")
    public String adminReviewList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
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

        // 처리가 끝나면보고 있던 페이지(인증 목록)로 리다이렉트
        return "redirect:/admin/certification/list";
    }

    // 리뷰 글 등록시 실거주인증 사진을 같이 보게 하기 위한 기능 추가
    // 이 부분은 ADMIN에서 관리하는 것이 좋을 지 고민 ( 사유 : ADMIN 에서만 보기 때문에 ADMIN 으로 둘지 고민 )
    // Review 에서 쓸지 고민되는 이유 ( 일단 DB는 새로 만든게 아니라 ,
    // 리뷰 등록하면 기본적으로 PENDING으로 가는데 , 이를 인증을 하면 공개됨
    @GetMapping("/certification/list")
    public String reviewCertificationList(Model model,
                                          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // AdminService를 통해 BANNED 리뷰만 가져옴
        Page<ReviewDTO> reviews = adminService.getPendingCertifications(pageable);

        model.addAttribute("reviews", reviews);
        return "/admin/reviewcertification";
    }

    @GetMapping("/certification/pending-certifications")
    @ResponseBody
    public ResponseEntity<Integer> pendingCertificationBadge() {
        int count = adminService.countPendingCertifications();
        return ResponseEntity.ok(count);
    }

    // [수정] 상세 페이지 매핑 변경: /detail/{id} -> /certification/confirm/{id}
    @GetMapping("/certification/confirm/{id}")
    public String adminReviewDetail(@PathVariable("id") Long id, Model model) {
        ReviewDTO reviewDTO = adminService.getAdminReviewDetail(id);
        model.addAttribute("reviewDTO", reviewDTO);
        return "admin/review_detail";
    }

    /*
    ====================================================================================================================
    REPORT(신고) 관리 기능
    ====================================================================================================================
     */
    // Report -> 신고한 리스트를 나오게 하는 법
    @GetMapping("/report/list")
    public String adminReportList(@RequestParam(value = "reportStatus", required = false) String reportStatus, Model model) {
        model.addAttribute("reports", reportService.findAllReports(reportStatus));
        model.addAttribute("currentFilter", reportStatus);
        return "admin/report/list";
    }

    // Report -> 신고 글을 삭제하는 방법
    @GetMapping("/report/delete/{id}")
    public String deleteReport(@PathVariable("id") Long id, RedirectAttributes rttr) {
        try {
            reportService.deleteReport(id);
            rttr.addFlashAttribute("message", "신고 내역이 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/report/list";
    }

    // 사용자 신고 기능 , 신고 글을 읽음 처리 , 읽지 않음 처리 확인하는 기능
    @GetMapping("/report/pending-reports")
    @ResponseBody
    public ResponseEntity<Integer> pendingReportBadge() {
        int count = reportService.countPendingReports();
        return ResponseEntity.ok(count);
    }

    // 상태 변경 기능(완료 처리)도 ReportController로 가져옵니다.
    @GetMapping("/report/complete/{id}")
    public String completeReport(@PathVariable("id") Long id, RedirectAttributes rttr) {
        reportService.updateReportStatus(id, "DONE");
        rttr.addFlashAttribute("message", "처리 완료로 변경되었습니다.");
        return "redirect:/admin/report/list";
    }
    // AdminController.java

//    @GetMapping("/post/edit/{id}")
//    public String editPostForm(@PathVariable("id") Long id, Model model) {
//        // 기존 게시글 정보 가져오기 (PostDTO 활용)
//        PostDTO postDTO = postService.getPostDetail(id);
//        model.addAttribute("post", postDTO);
//
//        // admin/postnotice.html을 재활용하거나 새로 만든 수정페이지로 연결
//        // 여기서는 수정을 위해 새로 만들 페이지명을 적습니다.
//        return "admin/post_edit";
//    }

    // status가 확인함 확인안함으로 나오도록 도와주는 기능
    @GetMapping("/report/toggleStatus/{id}")
    public String toggleReportStatus(@PathVariable("id") Long id,
                                     @RequestParam(value = "currentFilter", required = false) String currentFilter,
                                     RedirectAttributes rttr) {
        try {
            // 서비스의 토글 로직 호출
            reportService.toggleReportStatus(id);
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "상태 변경 중 오류가 발생했습니다.");
        }

        // 처리가 끝나면 원래 보고 있던 필터 조건을 유지하며 목록으로 돌아감
        String redirectUrl = "redirect:/admin/report/list";
        if (currentFilter != null && !currentFilter.isEmpty()) {
            redirectUrl += "?status=" + currentFilter;
        }
        return redirectUrl;
    }

    // 리뷰 실거주 인증 반려 사유 메시지 저장
    @PostMapping("/reviews/update-status")
    @ResponseBody
    public ResponseEntity<String> updateReviewStatus(
            @RequestParam Long reviewId,
            @RequestParam String status,
            @RequestParam(required = false) String message) {

        try {
            Status targetStatus = Status.valueOf(status.toUpperCase());

            // 반려(BANNED) 처리 시 메시지 검증
            if (targetStatus == Status.BANNED) {
                if (message == null || message.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("반려 사유(메시지)를 입력해야 합니다.");
                }
            }

            // 서비스 호출 (Review + Certification 상태를 동시에 업데이트)
            reviewService.processCertification(reviewId, targetStatus, message);

            return ResponseEntity.ok("처리가 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 상태 값입니다.");
        } catch (Exception e) {
            log.error("관리자 리뷰 승인/반려 처리 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    // 클래스 상단에 @RequestMapping("/admin")이 있으므로
    @PostMapping("/reviews/reject") // 실제 주소는 /admin/reviews/reject가 됩니다.
    @ResponseBody
    public ResponseEntity<String> rejectReview(@RequestParam Long reviewId, @RequestParam String message) {
        // adminService.rejectCertification(reviewId, message);
        // 위 메서드 대신 reviewService.processCertification을 사용하여 상태와 메시지를 함께 업데이트합니다.
        reviewService.processCertification(reviewId, Status.BANNED, message);
        return ResponseEntity.ok("반려 처리가 완료되었습니다.");
    }
}
