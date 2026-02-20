package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.domain.Review;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.CrimeStatsService;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReactionService reactionService;
    private final UserService userService;
    private final CrimeStatsService crimeStatsService;

    // [New] ★ 새로 만든 파일 유틸 서비스
    private final FileUtilService fileUtilService;


    // =====================================================================
    // 1. 리뷰 목록 조회
    // =====================================================================
    @GetMapping
    public String list(@PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) List<String> pros,
                       @RequestParam(required = false) List<String> cons,
                       Model model) {

        Page<ReviewDTO> reviews = reviewService.findAll(searchType, keyword, pros, cons, pageable);

        // 좋아요 표시
        for (ReviewDTO review : reviews) {
            int count = reactionService.countReaction("review", review.getId(), 1);
            review.setLikeCount(count);
        }

        // 지도 표시용 전체 리뷰
        List<ReviewDTO> allReviews = reviewService.findAll(searchType, keyword, pros, cons);

        // 장점/단점 체크박스 항목
        List<String> prosList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");
        List<String> consList = List.of("채광", "난방", "배수", "온수", "수압", "곰팡이", "해충", "소음", "치안", "집주인");

        model.addAttribute("reviews", reviews);
        model.addAttribute("allReviews", allReviews);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pros", pros);
        model.addAttribute("cons", cons);
        model.addAttribute("prosList", prosList);
        model.addAttribute("consList", consList);

        return "review/list";
    }

    // =====================================================================
    // 2. 리뷰 상세 조회
    // =====================================================================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ReviewDTO reviewDTO = reviewService.findById(id);

        crimeStatsService.analyzeCrimeForReview(reviewDTO);

        model.addAttribute("reviewDTO", reviewDTO);

        return "review/detail";
    }

    // =====================================================================
    // 3. 리뷰 작성
    // =====================================================================
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
                        @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        review.setUserId(user.getId());

        long savedId = reviewService.save(review, prosList, consList);

        return "redirect:/review/certification?reviewId=" + savedId;
    }

    // =====================================================================
    // 3-1. 리뷰 실거주 인증
    // =====================================================================
    // --- [실거주 인증 기능 추가  ---

    /**
     * 실거주 인증 신청 페이지로 이동.
     *
     * @return 인증 신청 폼 HTML 경로
     */
    @GetMapping("/certification")
    public String certificationForm(@RequestParam("reviewId") Long reviewId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 로그인한 사용자의 정보를 가져와서 모델에 담아줘야 HTML에서 ${user.address}를 쓸 수 있습니다.
        try {
            User user = (User) userDetails;
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
    public String edit(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        ReviewDTO reviewDTO = reviewService.findById(id);
        if (reviewDTO.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        model.addAttribute("reviewDTO", reviewDTO);
        return "review/edit-form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Review review,
                       @RequestParam("prosList") List<String> prosList,
                       @RequestParam("consList") List<String> consList,
                       @AuthenticationPrincipal User user) throws IOException {
        ReviewDTO original = reviewService.findById(id);
        if (original.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        review.setId(id);
        reviewService.update(review, prosList, consList);

        return "redirect:/review/detail/" + id;
    }

    // =====================================================================
    // 5. 리뷰 삭제
    // =====================================================================
    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ReviewDTO original = reviewService.findById(id);
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

    // 좋아요
    @PostMapping("/reaction")
    public String like(@RequestParam("targetId") Long targetId, @RequestParam("type") int type,
                       @AuthenticationPrincipal User user) {
        Reaction reaction = new Reaction();
        reaction.setTargetType("review");
        reaction.setTargetId(targetId);
        reaction.setUserId(user.getId());
        reaction.setType(type);

        reactionService.save(reaction);

        return "redirect:/review/detail/" + targetId;
    }
}