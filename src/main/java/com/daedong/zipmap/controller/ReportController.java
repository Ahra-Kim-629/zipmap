package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 신고하기 폼으로 이동
    @GetMapping("/report/write/{postId}")
    public String reportForm(@PathVariable("postId") Long postId, Model model){
        log.info("신고 페이지 이동 - 게시글 번호: {}", postId);
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setPostId(postId);
        model.addAttribute("reportDTO", reportDTO);
        model.addAttribute("postId", postId);
        return "report/reportForm";
    }

    // 2. 신고 접수 처리 (이 부분이 핵심!)
    @PostMapping("/report/submit")
    public String submitReport(@ModelAttribute ReportDTO reportDTO,
                               @RequestParam(value = "reportFile", required = false) MultipartFile file) {

        log.info("### [신고접수 시작] postId: {}", reportDTO.getPostId());

        // 💡 세션에서 직접 현재 유저 정보를 꺼내오는 가장 확실한 방법
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipalDetails) {
            UserPrincipalDetails userDetails = (UserPrincipalDetails) principal;
            reportDTO.setUserId(userDetails.getUser().getId());
            log.info("### [로그인유저 확인 성공] ID: {}, 이름: {}", reportDTO.getUserId(), userDetails.getUsername());
        } else {
            log.warn("### [로그인정보 없음] 로그인 페이지로 이동합니다.");
            return "redirect:/login";
        }

        // 이제 진짜 저장하러 갑니다!
        reportService.saveReport(reportDTO, file);
        log.info("### [DB 저장 프로세스 호출 완료]");

        return "redirect:/post/detail/" + reportDTO.getPostId() + "?success=report";
    }

    // 3. 관리자 목록
    @GetMapping("/admin/report/list")
    public String adminReportList(Model model) {
        model.addAttribute("reports", reportService.findAllReports());
        return "admin/reportList";
    }
} // <--- 이 마지막 괄호가 클래스의 끝입니다. 이 아래에 코드가 더 있으면 안 돼요!