package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {

    private final PostService postService;

    @GetMapping("/detail/{id}")
    public String boardDetail(@PathVariable Long id, Model model) {
        PostDTO boardDTO = postService.getPostDetail(id);
        model.addAttribute("board", boardDTO);

        return "board/detail";
    }

}
