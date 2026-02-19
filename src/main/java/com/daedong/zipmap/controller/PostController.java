package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.*;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.util.ReactionService;
import com.daedong.zipmap.util.RepliesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/board")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final RepliesService repliesService;
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
            int likeCount = reactionService.countReaction("post", post.getId(), 1);
            int dislikeCount = reactionService.countReaction("post", post.getId(), -1);

            post.setLikeCount(likeCount);
            post.setDislikeCount(dislikeCount);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("location", location);

        return "board/list";
    }

    @GetMapping("/detail/{id}")
    public String boardDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        PostDTO boardDTO = postService.getPostDetail(id);
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());


        Reaction reaction = new Reaction();
        reaction.setTargetId(id);
        reaction.setTargetType("post");
        if(user != null) {
            reaction.setUserId(user.getId());
        }

        // 내가 누른 반응(1, -1 혹은 0)을 가져오는 서비스 메서드
        int myReaction = reactionService.getMyReaction(reaction);
        model.addAttribute("myReaction", myReaction);


        // 좋아요, 싫어요 표시
        boardDTO.setLikeCount(reactionService.countReaction("post", id, 1));
        boardDTO.setDislikeCount(reactionService.countReaction("post", id, -1));

        model.addAttribute("board", boardDTO);

        model.addAttribute("currentUserId", user.getId());

        // 해당 게시글에 달린 댓글 목록 보여주기
        List<Reply> replyList = repliesService.getReplyList("board", id);
        model.addAttribute("replyList", replyList);


        return "board/detail";
    }

    @GetMapping("/write")
    public String write() {
        return "board/write_form";
    }

    //@RequestParam("file") List<MultipartFile> files

    @PostMapping("/write")
    public String write(@AuthenticationPrincipal User user, Post post ,RedirectAttributes rttr) {
        try {
            post.setUserId(user.getId());

            Long savedId = postService.write(post);

            // 2. 파일 이사 (이미지가 없을 때를 대비해 null 체크가 필요할 수 있음)
            if (post.getContent() != null && post.getContent().contains("src=")) {
                String newContent = fileUtilService.moveTempFilesToPermanent(post.getContent(), "POST", savedId);
                postService.updateContent(savedId, newContent);
            }

            rttr.addFlashAttribute("message", "글 작성이 완료되었습니다.");
            return "redirect:/board";
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("error", "글 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/board/write";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes rttr) {
        PostDTO postDTO = postService.getPostDetail(id);
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        if (!postDTO.getUserId().equals(user.getId())) {
            rttr.addFlashAttribute("message", "권한이 없습니다.");
            return "redirect:/board";
        }

        model.addAttribute("board", postDTO);
        return "board/edit_form";
    }


    // @RequestParam("file") List<MultipartFile> files
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, Post post, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes rttr) {
        try {
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());

            // 1. ★ 이미지 동기화 (temp -> post 이동, 삭제된 건 제거)
            String newContent = fileUtilService.updateImagesFromContent(post.getContent(), "POST", id);

            post.setId(id);
            post.setUserId(user.getId());
            post.setContent(newContent); // 경로 보정된 내용 넣기

            postService.update(post);

            rttr.addFlashAttribute("message", "글 수정이 완료되었습니다.");
            return "redirect:/board/detail/" + id;
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "글 수정 중 오류가 발생했습니다.");
            return "redirect:/board";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes rttr) {
        try {
            PostDTO postDTO = postService.getPostDetail(id);
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());
            if (!postDTO.getUserId().equals(user.getId())) {
                rttr.addFlashAttribute("message", "권한이 없습니다.");
                return "redirect:/board";
            }

            // ★ 파일 전체 삭제 (DB + 실제파일)
            fileUtilService.deleteFilesByTarget("POST", id);

            postService.delete(id);

            rttr.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/board";
    }

    // 좋아요 싫어요
    @PostMapping("/reaction")
    public String like(@RequestParam("targetId")Long targetId, @RequestParam("type") int type, @AuthenticationPrincipal User user){
        Reaction like = new Reaction();
        like.setTargetType("post");
        like.setTargetId(targetId);
        like.setUserId(user.getId());
        like.setType(type);

        reactionService.save(like);

        return "redirect:/board/detail/" + targetId;
    }

    // =====================================================================
    // 썸머노트 이미지 업로드 (Ajax) - 무조건 temp 폴더 사용
    // =====================================================================
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

    // =====================================================================
    // 썸머노트 이미지 삭제 (Ajax)
    // =====================================================================
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

