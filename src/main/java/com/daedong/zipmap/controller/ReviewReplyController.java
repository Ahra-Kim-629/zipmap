package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.ReviewReply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/review/reply")
@RequiredArgsConstructor
public class ReviewReplyController {
    private final ReviewReplyService reviewReplyService;

    // 리뷰 댓글 작성
    @PostMapping("/write")
    public String write(ReviewReply reviewReply, @AuthenticationPrincipal User user) {
        reviewReply.setUserId(user.getId());
        reviewReplyService.addReply(reviewReply);

        return "redirect:/review/detail/" + reviewReply.getReviewId();
    }

    // 리뷰 댓글 수정
    @PostMapping("/edit")
    public String edit(ReviewReply reviewReply) {
        reviewReplyService.updateReply(reviewReply);

        return "redirect:/review/detail/" + reviewReply.getReviewId();
    }

    // 리뷰 댓글 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam("replyId") Long replyId, @RequestParam("reviewId") Long reviewId) {
        reviewReplyService.deleteReply(replyId);

        return "redirect:/review/detail/" + reviewId;
    }
}
