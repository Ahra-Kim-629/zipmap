package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.PostService;
import com.daedong.zipmap.service.UserService;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String location,
                       Model model) {
        // 전체 게시판 게시글 리스트
        Page<PostDTO> posts = postService.findAll(searchType, keyword, category, location, pageable);
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
        model.addAttribute("board", boardDTO);
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUserId", user.getId());

        return "board/detail";
    }

    @GetMapping("/write")
    public String write() {
        return "board/write_form";
    }

    @PostMapping("/write")
    public String write(@AuthenticationPrincipal UserDetails userDetails, Post post, @RequestParam("file") List<MultipartFile> files, Model model) {
        try {
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());
            post.setUserId(user.getId());
            postService.write(post, files);
            model.addAttribute("message", "글 작성이 완료되었습니다.");
            return "redirect:/board";
        } catch (Exception e) {
            model.addAttribute("error", "글 작성 중 오류가 발생했습니다.");
            return "board/write_form";
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

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, Post post, @RequestParam("file") List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes rttr) {
        try {
            User user = (User) userService.loadUserByUsername(userDetails.getUsername());
            post.setId(id);
            post.setUserId(user.getId());
            postService.update(post, files);
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
            postService.delete(id);
            rttr.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/board";
    }

    // 좋아요 기능
    @PostMapping("/reaction")
    public String handleReaction(@RequestParam("postId") Long postId,
                                 @RequestParam("userId") String userId,
                                 @RequestParam("type") String type) {

        postService.saveOrUpdateReaction(postId, userId, type);

        return "redirect:/board/detail/" + postId;
    }
}
