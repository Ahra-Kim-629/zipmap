package com.daedong.zipmap.domain.review.controller;

import com.daedong.zipmap.domain.review.entity.Review;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.global.security.auth.UserPrincipalDetails;
import com.daedong.zipmap.global.gemini.GeminiService;
import com.daedong.zipmap.domain.interaction.reaction.service.ReactionService;
import com.daedong.zipmap.domain.review.service.ReviewService;
import com.daedong.zipmap.domain.subscription.service.SubscriptionService;
import com.daedong.zipmap.global.file.service.FileUtilService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    @GetMapping({"", "/list"})
    public String list(@PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false, name = "q") String q,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) List<String> pros,
                       @RequestParam(required = false) List<String> cons,
                       Model model,
                       @AuthenticationPrincipal UserPrincipalDetails user) {

        String searchKeyword = (q != null && !q.isEmpty()) ? q : keyword;

        Page<ReviewDTO> reviews = reviewService.findAll(searchType, keyword, pros, cons, pageable);


//        // 좋아요 표시
//        for (ReviewDTO review : reviews) {
//            review.setLikeCount(reactionService.countReaction("review", review.getId(), 1));
//        }
        // 2. 가로 슬라이더용 리스트 (페이징 X, 검색어 O 유지)
        List<ReviewDTO> sliderReviews = reviewService.findAll(searchType, searchKeyword, pros, cons);

        // 3. 카카오맵 핀 전용 리스트 (페이징 X, 검색어 무시 -> null, null)
        List<ReviewDTO> mapReviews = reviewService.findAll(null, null, pros, cons);

        // 장점/단점 체크박스 항목
        List<String> prosList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");
        List<String> consList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");

        model.addAttribute("reviews", reviews);
        model.addAttribute("sliderReviews", sliderReviews);
        model.addAttribute("mapReviews", mapReviews);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("pros", pros);
        model.addAttribute("cons", cons);
        model.addAttribute("prosList", prosList);
        model.addAttribute("consList", consList);

        //log.info("검색 요청 감지 - q: {}, keyword: {}, 최종결정: {}", q, keyword, searchKeyword);

        // 로그인한 사용자의 구독 목록
        if (user != null) {
            List<String> myKeywords = subscriptionService.getMyKeywords(user.getMember().getId(), "review");
            model.addAttribute("myKeywords", myKeywords);
        }

        return "review/list";
    }

    // 리뷰 상세 조회
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal UserPrincipalDetails user,
                         HttpServletRequest request) {
        ReviewDTO reviewDTO = reviewService.getReviewDetail(id, request, user);

        Integer isLiked = null;
        // [수정 포인트] user가 null인지 먼저 확인하여 NullPointerException 방지
        if (user != null && user.getMember() != null) {
            isLiked = reactionService.getReactionByUserId(user.getMember().getId(), id, "review");
        }

        model.addAttribute("reviewDTO", reviewDTO); // 여기서 객체 이름이 'reviewDTO'인 것을 기억하세요.
        model.addAttribute("isLiked", isLiked);

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
                        @AuthenticationPrincipal UserPrincipalDetails user) throws IOException {
        review.setUserId(user.getMember().getId());

        long savedId = reviewService.write(review, prosList, consList);

        return "redirect:/review/certification?reviewId=" + savedId;
    }

    // 리뷰 실거주 인증
    @GetMapping("/certification")
    public String certificationForm(@RequestParam("reviewId") Long reviewId,
                                    Model model,
                                    @AuthenticationPrincipal UserPrincipalDetails user) {
        try {
            // 1. 기존 유저 정보 및 리뷰 ID 담기
            model.addAttribute("user", user.getMember());
            model.addAttribute("reviewId", reviewId);

            // 2. ✨ 가장 안전한 방법: 리뷰 상세를 통째로 가져오지 말고 필요한 '사유'만 별도로 조회
            // (이미 ReviewDTO에 message 필드를 만드셨으니, 서비스에서 간단한 조회 메서드를 하나 쓰거나 DTO를 직접 받으세요)
            ReviewDTO review = reviewService.getReviewDetail(reviewId);

            if (review != null) {
                // 사유(message)와 현재 상태를 모델에 추가
                model.addAttribute("rejectMessage", review.getMessage());
                model.addAttribute("reviewStatus", review.getReviewStatus().name());

                //  [추가된 핵심 코드] 방금 작성한 리뷰의 주소를 화면에 전달
                model.addAttribute("reviewAddress", review.getAddress());
            }

            return "/users/certification";
        } catch (Exception e) {
            log.error("인증 폼 로딩 중 오류 발생: ", e);
            return "redirect:/review";
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
                                      @AuthenticationPrincipal UserPrincipalDetails user,
                                      RedirectAttributes rttr) {
        try {
            // 1. UserService에 만든 파일 저장 로직을 실행.
            // (파일을 하드디스크에 저장하고 DB에 기록하는 기능)
            reviewService.registerCertification(user.getMember(), file, reviewId);

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
    public String edit(@PathVariable Long id,
                       Model model,
                       @AuthenticationPrincipal UserPrincipalDetails user,
                       RedirectAttributes rttr) {
        ReviewDTO reviewDTO = reviewService.getReviewDetail(id);
        if (reviewDTO.getUserId() != user.getMember().getId()) {
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
                       @AuthenticationPrincipal UserPrincipalDetails user,
                       RedirectAttributes rttr) {
        //1. 기존 데이터 조회 ( 권한 확인 및 상태 체크용도로 사용 )
        ReviewDTO original = reviewService.getReviewDetail(id);
        //2. 권한 체크
        if (original.getUserId() != user.getMember().getId()) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/review/detail/" + id;
        }
        //3. 업데이트 ( 리뷰 업데이트 수행 )
        review.setId(id);
        reviewService.update(review, prosList, consList);

        // 4. ✨ 리다이렉트 분기 로직 추가
        // 수정 전 상태가 BANNED(인증 반려)였다면 다시 인증 서류 페이지로 보냄
        if (original.getReviewStatus() != null &&
                ("BANNED".equals(original.getReviewStatus().name()) || "PENDING".equals(original.getReviewStatus().name()))){
            rttr.addFlashAttribute("successMessage", "리뷰 수정 완료! 이제 증빙 서류를 다시 제출해 주세요.");
            return "redirect:/review/certification?reviewId=" + id;
        }

        rttr.addFlashAttribute("successMessage", "리뷰가 성공적으로 수정되었습니다.");
        return "redirect:/review/detail/" + id;
    }

    // =====================================================================
    // 5. 리뷰 삭제
    // =====================================================================
    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipalDetails user) {
        ReviewDTO original = reviewService.getReviewDetail(id);
        if (original.getUserId() != user.getMember().getId()) {
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