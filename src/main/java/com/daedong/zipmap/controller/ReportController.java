package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReportDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 신고하기 폼으로 이동 (통합)
    @GetMapping("/report/write/{targetType}/{targetId}")
    public String reportForm(@PathVariable("targetType") String targetType,
                             @PathVariable("targetId") Long targetId,
                             Model model){
        log.info("신고 페이지 이동 - 유형: {}, 번호: {}", targetType, targetId);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setTargetType(targetType.toUpperCase()); // POST or REVIEW
        reportDTO.setTargetId(targetId);

        model.addAttribute("reportDTO", reportDTO);
        return "report/reportForm";
    }

    // 2. 신고 접수 처리
    @PostMapping("/report/submit")
    public String submitReport(@ModelAttribute ReportDTO reportDTO,
                               @RequestParam(value = "reportFile", required = false) MultipartFile file,
                               @AuthenticationPrincipal User user) {

        if (user == null) {
            return "redirect:/login";
        }

        log.info("신고 접수 - 유형: {}, ID: {}", reportDTO.getTargetType(), reportDTO.getTargetId());

        reportDTO.setUserId(user.getId());
        reportService.saveReport(reportDTO, file);

        // 리다이렉트 경로 분기
        if ("REVIEW".equals(reportDTO.getTargetType())) {
            return "redirect:/review/detail/" + reportDTO.getTargetId() + "?success=report";
        } else {
            return "redirect:/post/detail/" + reportDTO.getTargetId() + "?success=report";
        }
    }

    // 3. 관리자 목록
    @GetMapping("/admin/report/list")
    public String adminReportList(Model model) {
        model.addAttribute("reports", reportService.findAllReports());
        return "admin/reportList";
    }

    // 4. 신고 삭제 기능 추가
    @GetMapping("/admin/report/delete/{id}")
    public String deleteReport(@PathVariable("id") Long id, RedirectAttributes rttr) {
        try {
            reportService.deleteReport(id);
            rttr.addFlashAttribute("message", "신고 내역이 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/report/list";
    }
}