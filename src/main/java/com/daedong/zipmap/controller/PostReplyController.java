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
//    @PostMapping("/replies/delete/{id}")
//    public String deleteReply(@PathVariable("id") Long id, @RequestParam("postId") Long postId) {
//        // 이제 주소창에 입력해도 이 메서드는 실행되지 않습니다! (POST 요청만 받기 때문)
//        postReplyService.deleteReply(id);
//        return "redirect:/board/detail/" + postId;
//    }
    @PostMapping("/delete/{id}")
    public String deleteReply(@PathVariable("id") Long id,
                              @RequestParam("postId") Long postId,
                              java.security.Principal principal,
                              org.springframework.security.core.Authentication auth) {

        if (principal == null) return "redirect:/login"; // 로그인 안 했으면 로그인으로

        String currentUserId = principal.getName();

        // 관리자 권한 확인 (ROLE_ADMIN)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            postReplyService.deleteReply(id, currentUserId, isAdmin);
        } catch (Exception e) {
            // 권한 부족 시 에러 메시지 처리 (선택 사항)
            return "redirect:/board/detail/" + postId + "?error=denied";
        }

        return "redirect:/board/detail/" + postId;
    }

    // 댓글 수정 요청
    @PostMapping("/edit")
    public String editReply(PostReply reply) {
        postReplyService.updateReply(reply);
        return "redirect:/board/detail/" + reply.getPostId();
    }
}