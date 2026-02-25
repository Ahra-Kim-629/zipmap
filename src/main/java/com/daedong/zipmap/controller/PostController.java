package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.GeminiService;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.ReactionService;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.util.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final ReplyService replyService;
    private final ReactionService reactionService;
    private final GeminiService geminiService; // GeminiService 주입 추가

    private final FileUtilService fileUtilService;

    @GetMapping
    public String list(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String location,
                       Model model) {
        // 전체 게시판 게시글 리스트
        Page<PostDTO> posts = postService.findAll(searchType, keyword, category, location, pageable);

        // 좋아요 싫어요 표시
        for (PostDTO post : posts.getContent()) {
            post.setLikeCount(reactionService.countReaction("post", post.getId(), 1));
            post.setDislikeCount(reactionService.countReaction("post", post.getId(), -1));
        }

        model.addAttribute("posts", posts);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("location", location);

        return "post/list";
    }

    @GetMapping("/detail/{id}")
    public String boardDetail(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal User user,
                              HttpServletRequest request) {
        PostDTO postDTO = postService.getPostDetail(id, request, user);

        model.addAttribute("postDTO", postDTO);

        return "post/detail";
    }

    @GetMapping("/write")
    public String write() {
        return "post/write-form";
    }

    @PostMapping("/write")
    public String write(@AuthenticationPrincipal User user, Post post, @RequestParam(value = "file", required = false) MultipartFile file, RedirectAttributes rttr) {
        try {
            post.setUserId(user.getId());

            Long savedId = postService.write(post);

            // 파일 이사 (이미지가 없을 때를 대비해 null 체크가 필요할 수 있음)
            if (post.getContent() != null && post.getContent().contains("src=")) {
                String newContent = fileUtilService.moveTempFilesToPermanent(post.getContent(), "POST", savedId);
                postService.updateContent(savedId, newContent);
            }

            // 2. 일반 첨부파일 처리 (file input으로 들어온 파일)
            if (file != null && !file.isEmpty()) {
                String filePath = fileUtilService.saveFile(file, "post"); // post 폴더에 저장

                // DB에 파일 정보 저장
                com.daedong.zipmap.domain.File fileEntity = new com.daedong.zipmap.domain.File();
                fileEntity.setTargetType("POST");
                fileEntity.setTargetId(savedId);
                fileEntity.setFilePath(filePath);
                fileEntity.setFileSize(file.getSize());
                
                // FileMapper를 직접 호출하거나 Service를 통해 저장해야 함.
                // 여기서는 FileUtilService에 저장 로직이 없으므로, FileUtilService에 메서드를 추가하거나
                // PostService를 통해 저장하도록 수정해야 합니다. 
                // 하지만 FileUtilService.moveTempFilesToPermanent 내부에서 fileMapper.insertFile을 호출하고 있으므로
                // 유사하게 처리할 수 있는 메서드를 FileUtilService에 추가하는 것이 좋습니다.
                // 일단 여기서는 FileUtilService에 saveFileAndInsertDB 메서드를 추가했다고 가정하거나
                // 기존 saveFile 메서드 호출 후 별도로 DB 저장을 수행해야 합니다.
                
                // 임시 방편: FileUtilService에 의존하지 않고 직접 매퍼를 부를 수 없으니
                // PostService에 파일 저장 위임 메서드를 만드는 것이 정석이나,
                // 현재 구조상 FileUtilService를 수정하여 DB 저장까지 처리하도록 유도하겠습니다.
                // (아래 FileUtilService 수정 참고)
                fileUtilService.saveFileToDB(fileEntity); 
            }

            rttr.addFlashAttribute("message", "글 작성이 완료되었습니다.");
            return "redirect:/post";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "글 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/post/write";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal User user, Model model, RedirectAttributes rttr) {
        PostDTO postDTO = postService.getPostDetail(id);
        if (!postDTO.getUserId().equals(user.getId())) {
            rttr.addFlashAttribute("message", "권한이 없습니다.");
            return "redirect:/post";
        }

        model.addAttribute("post", postDTO);
        return "post/edit-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, Post post, @AuthenticationPrincipal User user, RedirectAttributes rttr) {
        try {
            // 1. ★ 이미지 동기화 (temp -> post 이동, 삭제된 건 제거)
            String newContent = fileUtilService.updateImagesFromContent(post.getContent(), "POST", id);

            post.setId(id);
            post.setUserId(user.getId());
            post.setContent(newContent); // 경로 보정된 내용 넣기

            postService.update(post);

            rttr.addFlashAttribute("message", "글 수정이 완료되었습니다.");
            return "redirect:/post/detail/" + id;
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "글 수정 중 오류가 발생했습니다.");
            return "redirect:/post";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes rttr) {
        PostDTO postDTO = postService.getPostDetail(id);
        if (!postDTO.getUserId().equals(user.getId())) {
            rttr.addFlashAttribute("message", "권한이 없습니다.");
            return "redirect:/post";
        }

        try {
            postService.delete(id);
            rttr.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/post";
    }

    // 썸머노트 이미지 업로드 (Ajax) - 무조건 temp 폴더 사용
    @PostMapping("/uploadSummernoteImage")
    @ResponseBody
    public Map<String, Object> uploadSummernoteImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            String filePath = fileUtilService.saveTempImage(file);
            response.put("url", "/files/" + filePath);
            response.put("responseCode", "success");
        } catch (IOException e) {
            response.put("responseCode", "error");
        }
        return response;
    }

    // 썸머노트 이미지 삭제 (Ajax)
    @PostMapping("/deleteSummernoteImage")
    @ResponseBody
    public String deleteSummernoteImage(@RequestParam("src") String src) {
        try {
            String filePath = src;
            if (src.contains("/files/")) {
                filePath = src.split("/files/")[1];
            }
            fileUtilService.deleteFileByPath(filePath);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    // 카테고리별 AI 요약 요청 처리
    @GetMapping("/summarize-category")
    @ResponseBody
    public String summarizeCategory(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String location,
                                    @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // 현재 필터 조건에 맞는 게시글 목록 조회 (최신 10개만)
        Page<PostDTO> posts = postService.findAll("title", keyword, category, location, pageable);
        
        // GeminiService를 통해 요약 요청
        return geminiService.summarizeCategory(category, posts.getContent());
    }

    // [추가] 선택된 게시글 요약 요청 처리
    @PostMapping("/summarize-selected")
    @ResponseBody
    public String summarizeSelected(@RequestBody List<Long> postIds) {
        // 선택된 ID로 게시글 조회
        List<PostDTO> posts = postService.getPostsByIds(postIds);
        
        // GeminiService를 통해 요약 요청
        return geminiService.summarizeSelectedPosts(posts);
    }

    // [추가] 상세 페이지 AI 요약 요청 처리
    @GetMapping("/summarize-detail/{id}")
    @ResponseBody
    public String summarizeDetail(@PathVariable Long id, HttpServletRequest request, @AuthenticationPrincipal User user) {
        // 기존 상세조회 로직 재사용
        PostDTO postDTO = postService.getPostDetail(id, request, user);

        // postDTO에 담긴 댓글 리스트와 함께 Gemini 호출
        return geminiService.summarizePostDetail(postDTO);
    }

}