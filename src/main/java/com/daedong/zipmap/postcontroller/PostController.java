package com.daedong.zipmap.postcontroller;

import com.daedong.zipmap.postdomain.Post;
import com.daedong.zipmap.postservice.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "searchType", required = false) String searchType,
                       @RequestParam(value = "key", required = false) String key){
        // 전체 게시판 게시글 리스트
        List<Post> posts = postService.findAll(searchType, key);
        model.addAttribute("posts", posts);

        return "list";
    }

}
