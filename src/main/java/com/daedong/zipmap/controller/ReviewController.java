package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.ReviewService;
import com.daedong.zipmap.util.FileUtilService; // ★ (신) 만능 파일 서비스
import com.daedong.zipmap.util.LikesService;
import com.daedong.zipmap.util.RepliesService;
import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final RepliesService repliesService;
    private final LikesService likesService;

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
        for(ReviewDTO review : reviews) {
            int count = likesService.countLikes("review", review.getId(), 1);
            review.setLikeCount(count);
        }

        // 지도 표시용 전체 리뷰
        List<ReviewDTO> allReviews = reviewService.findAll(searchType, keyword, pros, cons);

        // 좋아요 표시
        for(ReviewDTO review : allReviews) {
            int count = likesService.countLikes("review", review.getId(), 1);
            review.setLikeCount(count);
        }

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

        // 좋아요
        int likeCount = likesService.countLikes("review", id, 1);
        reviewDTO.setLikeCount(likeCount);

        List<Replies> replyList = repliesService.getReplyList("review", id);
        model.addAttribute("replyList", replyList);

        model.addAttribute("reviewDTO", reviewDTO);

        return "review/detail";
    }

    // =====================================================================
    // 3. 리뷰 작성
    // =====================================================================
    @GetMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write() {
        return "review/writeForm";
    }

    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String write(ReviewDTO reviewDTO,
                        @AuthenticationPrincipal User user) throws IOException {

        reviewDTO.setUserId(user.getId());

        // 1. 글 내용 저장 (먼저 저장해야 ID가 생성됨)
        Long savedId = reviewService.save(reviewDTO);

        // 2. [핵심] temp 폴더 -> review 폴더로 파일 이사
        // 리턴값(newContent): 이미지 경로가 "/files/review/..."로 변경된 HTML
        String newContent = fileUtilService.moveTempFilesToPermanent(reviewDTO.getContent(), "REVIEW", savedId);

        // 3. 바뀐 HTML 내용으로 DB 업데이트
        reviewService.updateContent(savedId, newContent);

        return "redirect:/review/detail/" + savedId;
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
        return "review/editForm";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id,
                       ReviewDTO reviewDTO,
                       @AuthenticationPrincipal User user) throws IOException {

        ReviewDTO original = reviewService.findById(id);
        if (original.getUserId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        // 1. 이미지 처리 (temp -> review 이동 및 삭제 처리)
        // 리턴값(newContent): 경로가 정리된 HTML
        String newContent = fileUtilService.updateImagesFromContent(reviewDTO.getContent(), "REVIEW", id);

        // 2. 정리된 내용으로 글 수정
        reviewDTO.setId(id);
        reviewDTO.setUserId(user.getId());
        reviewDTO.setContent(newContent); // 경로 보정된 내용 넣기

        reviewService.edit(reviewDTO);

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
        fileUtilService.deleteFilesByTarget("REVIEW", id);
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
            e.printStackTrace();
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
    public String like(@RequestParam("targetId")Long targetId, @RequestParam("type") int type, @AuthenticationPrincipal User user){
        Likes like = new Likes();
        like.setTargetType("review");
        like.setTargetId(targetId);
        like.setUserId(user.getId());
        like.setType(type);

        likesService.save(like);

        return "redirect:/review/detail/" + targetId;
    }

}