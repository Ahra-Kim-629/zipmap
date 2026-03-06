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
                             @AuthenticationPrincipal UserPrincipalDetails user,
                             Model model, RedirectAttributes rttr) {

        if (reportService.hasUserAlreadyReported(user.getUser().getId(), targetType, targetId)) {
            rttr.addFlashAttribute("errorMessage", "이미 신고한 항목입니다.");
            return "redirect:/" + targetType.toLowerCase() + "/detail/" + targetId;
        }

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
                               @AuthenticationPrincipal UserPrincipalDetails user,
                               RedirectAttributes rttr) {

        if (reportService.hasUserAlreadyReported(user.getUser().getId(), reportDTO.getTargetType(), reportDTO.getTargetId())) {
            rttr.addFlashAttribute("errorMessage", "이미 신고가 접수된 항목입니다.");
            return "redirect:/" + reportDTO.getTargetType().toLowerCase() + "/detail/" + reportDTO.getTargetId();
        }

        log.info("신고 접수 - 유형: {}, ID: {}", reportDTO.getTargetType(), reportDTO.getTargetId());

        reportDTO.setUserId(user.getUser().getId());
        reportService.saveReport(reportDTO, file);

        // 리다이렉트 경로 분기
        if ("REVIEW".equals(reportDTO.getTargetType())) {
            return "redirect:/review/detail/" + reportDTO.getTargetId() + "?success=report";
        } else {
            return "redirect:/post/detail/" + reportDTO.getTargetId() + "?success=report";
        }
    }
}