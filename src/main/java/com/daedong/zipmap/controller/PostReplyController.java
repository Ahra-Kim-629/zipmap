package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.PostReply;
import com.daedong.zipmap.service.PostReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/replies")
public class PostReplyController {
    private final PostReplyService postReplyService;

    @PostMapping("/add")
    public String addReply(PostReply reply) {
        // 1. 댓글 저장 수행
        postReplyService.saveReply(reply);

        // 2. 저장이 끝나면 다시 보던 게시글 상세 페이지로 돌아갑니다 (Redirect)
        return "redirect:/board/detail/" + reply.getPostId();
    }
}