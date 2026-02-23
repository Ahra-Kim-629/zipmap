package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.domain.UserPrincipalDetails; // 종빈님 도메인에 맞게 수정
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
//@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 신고하기 폼으로 이동
    @GetMapping("/write/{postId}")
    public String reportForm(@PathVariable("postId") Long postId, Model model){
        log.info("신고 페이지 이동 - 게시글 번호: {}", postId);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setPostId(postId);

        model.addAttribute("reportDTO", reportDTO);
        model.addAttribute("postId", postId);
        return "report/reportForm";
    }

    // 2. 신고 접수 처리
    @PostMapping("/submit")
    public String submitReport(@ModelAttribute ReportDTO reportDTO,
                               @RequestParam(value = "reportFile", required = false) MultipartFile file,
                               @AuthenticationPrincipal UserPrincipalDetails principal){ // UserPrincipalDetails 사용

        // 1. 로그인 체크 및 사용자 ID 설정
        if (principal != null && principal.getUser() != null) {
            reportDTO.setUserId(principal.getUser().getId());
            log.info("신고자 아이디 세팅 완료: {}", reportDTO.getUserId());
        } else {
            log.warn("로그인 정보가 없습니다.");
            return "redirect:/login";
        }

        // 2. 서비스 호출
        try {
            reportService.saveReport(reportDTO, file);
        } catch (Exception e) {
            log.error("신고 저장 중 오류 발생: {}", e.getMessage());
            return "redirect:/post/detail/" + reportDTO.getPostId() + "?error=report_failed";
        }

        return "redirect:/post/detail/" + reportDTO.getPostId() + "?success=report";
    }

    @GetMapping("/admin/report/list")
    public String adminReportList(Model model) {
        log.info("관리자 신고 목록 페이지 접속");

        // 서비스에서 모든 신고 내역을 가져옵니다.
        List<ReportDTO> reportList = reportService.findAllReports();

        model.addAttribute("reports", reportService.findAllReports());

        // templates/admin/reportList.html 파일을 찾아서 보여줍니다.
        return "admin/reportList";
    }

}