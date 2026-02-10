package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {
    private final PostService postService;

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

    @PostMapping("/wirte")
    public String write(Post post, @RequestParam("file") MultipartFile file, Model model) {
        try {
            postService.write(post, file);
            model.addAttribute("message", "글 작성이 완료되었습니다.");
            model.addAttribute("searchType", "");
            model.addAttribute("keyword", "");
            return "redirect:/board";
        } catch (Exception e) {
            model.addAttribute("error", "글 작성 중 오류가 발생했습니다.");
            return "board/write_form";
        }
    }
}
