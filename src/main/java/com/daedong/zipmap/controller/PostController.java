package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.postservice.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class PostController {

    private final PostService postService;


    @GetMapping
    public String list(Model model) {
        // 전체 게시판 게시글 리스트
        List<Post> posts = postService.findAll(null, null);
        model.addAttribute("posts", posts);

        return "post/list";
    }

    @GetMapping
    public String list(@Pageable(size = 3, sort = "id", direction = Sort.Direction.DESC)
                       @RequestParam(required = false)String searchType,
                       @RequestParam(required = false) String keyword, Model model){
        List<Post> posts = postService.findAll(searchType,keyword);
        model.addAttribute("posts",posts);
        model.addAttribute("searchType",searchType);
        model.addAttribute("keyword",keyword);
        return "posts/list";
    }



}

