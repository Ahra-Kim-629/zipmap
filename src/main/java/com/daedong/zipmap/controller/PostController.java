package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
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
    public String write(@AuthenticationPrincipal User user, Post post, RedirectAttributes rttr) {
        try {
            post.setUserId(user.getId());

            Long savedId = postService.write(post);

            // 2. 파일 이사 (이미지가 없을 때를 대비해 null 체크가 필요할 수 있음)
            if (post.getContent() != null && post.getContent().contains("src=")) {
                String newContent = fileUtilService.moveTempFilesToPermanent(post.getContent(), "POST", savedId);
                postService.updateContent(savedId, newContent);
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


}

