package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.PostReply;
import com.daedong.zipmap.service.PostReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    // 댓글 삭제 요청
    @GetMapping("/delete/{id}")
    public String deleteReply(@PathVariable Long id, @RequestParam Long postId) {
        postReplyService.deleteReply(id);
        return "redirect:/board/detail/" + postId; // 삭제 후 다시 상세 페이지로
    }

    // 댓글 수정 요청
    @PostMapping("/edit")
    public String editReply(PostReply reply) {
        postReplyService.updateReply(reply);
        return "redirect:/board/detail/" + reply.getPostId();
    }
}