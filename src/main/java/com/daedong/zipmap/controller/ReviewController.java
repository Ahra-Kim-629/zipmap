package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.SubscriptionRequest;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.GeminiService;
import com.daedong.zipmap.service.ReactionService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.SubscriptionService;
import com.daedong.zipmap.util.FileUtilService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j // 로그 어노테이션 추가
@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReactionService reactionService;
    private final FileUtilService fileUtilService;
    private final SubscriptionService subscriptionService;


    private final GeminiService geminiService;

    // 리뷰 목록 조회
    @GetMapping({"","/list"})
    public String list(@PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false, name="q") String q,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) List<String> pros,
                       @RequestParam(required = false) List<String> cons,
                       Model model,
                       @AuthenticationPrincipal User user) {


        Page<ReviewDTO> reviews = reviewService.findAll(searchType, keyword, pros, cons, pageable);

        String searchKeyword = (q != null && !q.isEmpty()) ? q : keyword;

//        // 좋아요 표시
//        for (ReviewDTO review : reviews) {
//            review.setLikeCount(reactionService.countReaction("review", review.getId(), 1));
//        }

        // 지도 표시용 전체 리뷰
        List<ReviewDTO> allReviews = reviewService.findAll(searchType, searchKeyword, pros, cons);

        // 장점/단점 체크박스 항목
        List<String> prosList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");
        List<String> consList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");

        model.addAttribute("reviews", reviews);
        model.addAttribute("allReviews", allReviews);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("pros", pros);
        model.addAttribute("cons", cons);
        model.addAttribute("prosList", prosList);
        model.addAttribute("consList", consList);

        //log.info("검색 요청 감지 - q: {}, keyword: {}, 최종결정: {}", q, keyword, searchKeyword);

        // 로그인한 사용자의 구독 목록
        if (user != null) {
            List<String> myKeywords = subscriptionService.getMyKeywords(user.getId(), "review");
            model.addAttribute("myKeywords", myKeywords);
        }

        return "review/list";
    }

    // 리뷰 상세 조회
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         HttpServletRequest request) {
        ReviewDTO reviewDTO = reviewService.getReviewDetail(id, request, userDetails);

        model.addAttribute("reviewDTO", reviewDTO);

        return "review/detail";
    }

    // 리뷰 작성
    @GetMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write() {
        return "review/write-form";
    }

    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write(Review review,
                        @RequestParam("prosList") List<String> prosList,
                        @RequestParam("consList") List<String> consList,
                        @AuthenticationPrincipal User user) throws IOException {
        review.setUserId(user.getId());

        long savedId = reviewService.write(review, prosList, consList);

        return "redirect:/review/certification?reviewId=" + savedId;
    }

    // 리뷰 실거주 인증
    @GetMapping("/certification")
    public String certificationForm(@RequestParam("reviewId") Long reviewId, Model model, @AuthenticationPrincipal User user) {
        // 로그인한 사용자의 정보를 가져와서 모델에 담아줘야 HTML에서 ${user.address}를 쓸 수 있습니다.
        try {
            model.addAttribute("user", user);
            model.addAttribute("reviewId", reviewId);
            return "/users/certification";
        } catch (Exception e) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 페이지로
        }
    }

    /**
     * 사용자가 업로드한 임대차계약서 파일을 처리.
     *
     * @param file 사용자가 선택한 파일 (MultipartFile)
     * @param user 현재 로그인한 유저 정보 (Spring Security)
     * @param rttr 화면에 일회성 메시지를 전달하기 위한 객체
     * @return 처리가 완료된 후 이동할 주소
     */
    @PostMapping("/certification")
    public String submitCertification(@RequestParam("contractFile") MultipartFile file,
                                      @RequestParam("reviewId") Long reviewId,
                                      @AuthenticationPrincipal User user,
                                      RedirectAttributes rttr) {
        try {
            // 1. UserService에 만든 파일 저장 로직을 실행.
            // (파일을 하드디스크에 저장하고 DB에 기록하는 기능)
            reviewService.registerCertification(user, file, reviewId);

            // 2. 성공 메시지를 담아서 마이페이지로 보냄.
            rttr.addFlashAttribute("message", "인증 신청 완료! 관리자 승인 후 리뷰가 공개됩니다.");
            return "redirect:/review";

        } catch (Exception e) {
            // 에러가 발생하면 에러 메시지를 담아 다시 인증 페이지로 보냄.
            e.printStackTrace();
            rttr.addFlashAttribute("error", "인증 신청 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/review/certification?reviewId=" + reviewId;
        }
    }

    // =====================================================================
    // 4. 리뷰 수정
    // =====================================================================
    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Model model, @AuthenticationPrincipal User user, RedirectAttributes rttr) {
        ReviewDTO reviewDTO = reviewService.getReviewDetail(id);
        if (reviewDTO.getUserId() != user.getId()) {
            rttr.addFlashAttribute("errorMessage", "본인이 작성한 글만 수정할 수 있습니다.");
            return "redirect:/review/detail/" + id;
        }
        model.addAttribute("reviewDTO", reviewDTO);
        return "review/edit-form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Review review,
                       @RequestParam("prosList") List<String> prosList,
                       @RequestParam("consList") List<String> consList,
                       @AuthenticationPrincipal User user,
                       RedirectAttributes rttr) {
        ReviewDTO original = reviewService.getReviewDetail(id);
        if (original.getUserId() != user.getId()) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/review/detail/" + id;
        }

        review.setId(id);
        reviewService.update(review, prosList, consList);

        rttr.addFlashAttribute("successMessage", "리뷰가 성공적으로 수정되었습니다.");
        return "redirect:/review/detail/" + id;
    }

    // =====================================================================
    // 5. 리뷰 삭제
    // =====================================================================
    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ReviewDTO original = reviewService.getReviewDetail(id);
        if (original.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        // [New] 해당 리뷰와 관련된 모든 파일(DB+실제파일) 삭제
        reviewService.deleteReviewById(id);

        return "redirect:/review";
    }

    // =====================================================================
    // 6. 썸머노트 업로드 (Ajax) - 무조건 temp 폴더 사용
    // =====================================================================
    @PostMapping("/uploadSummernoteImage")
    @ResponseBody
    public Map<String, Object> uploadSummernoteImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ★ [핵심] 작성 중에는 무조건 'temp' 폴더에 저장합니다.
            String filePath = fileUtilService.saveTempImage(file);

            response.put("url", "/files/" + filePath);
            response.put("responseCode", "success");
        } catch (IOException e) {
            response.put("responseCode", "error");
        }
        return response;
    }

    // =====================================================================
    // 7. 썸머노트 이미지 삭제 (Ajax)
    // =====================================================================
    @PostMapping("/deleteSummernoteImage")
    @ResponseBody
    public String deleteSummernoteImage(@RequestParam("src") String src) {
        try {
            String filePath = src;
            if (src.contains("/files/")) {
                filePath = src.split("/files/")[1];
            }
            // 임시 파일이든 실제 파일이든 경로만 맞으면 지워줍니다.
            fileUtilService.deleteFileByPath(filePath);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    /**
     * [AI 기능] 화면에서 지역명을 주면, 요약본 텍스트를 리턴합니다.
     */
    @ResponseBody
    @GetMapping("/ai-summary")
    public String getAiSummary(@RequestParam("region") String region) {
        log.info("AI 리뷰 요약 요청 - 지역: {}", region); // 로그 추가

        List<String> reviewContents = reviewService.findContentsByRegion(region);

        if (reviewContents == null || reviewContents.isEmpty()) {
            log.info("해당 지역 리뷰 없음: {}", region);
            return region + "에는 아직 등록된 리뷰가 부족해서 AI가 분석할 수 없어요! 😅 조금 더 기다려주세요.";
        }

        // 💡 HTML 태그 제거 및 텍스트 정제 (속도 향상 및 AI 정확도 상승)
        java.util.List<String> cleanContents = new java.util.ArrayList<>();
        for (String content : reviewContents) {
            String cleanText = org.jsoup.Jsoup.parse(content).text();
            if (cleanText.length() > 300) {
                cleanText = cleanText.substring(0, 300);
            }
            cleanContents.add(cleanText);
        }

        return geminiService.summarizeReviews(region, cleanContents);
    }

}