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

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public String list(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        // 전체 게시판 게시글 리스트
        Page<Post> posts = postService.findAll(searchType, keyword, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "board/list";
    }

    @GetMapping("/detail/{id}")
    public String boardDetail(@PathVariable Long id, Model model) {
        PostDTO boardDTO = postService.getPostDetail(id);
        model.addAttribute("board", boardDTO);

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
}
