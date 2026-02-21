package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.util.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/write")
    public String write(Reply reply, @AuthenticationPrincipal User user) {
        reply.setUserId(user.getId());

        replyService.saveReply(reply);

        return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();
    }

    // 댓글 수정
    @PostMapping("/edit")
    public String edit(Reply reply) {
        replyService.updateReply(reply);

        return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();
    }

    // 댓글 삭제
    @PostMapping("/delete")
    public String delete(Reply reply, String targetType, @AuthenticationPrincipal User user) {
        if (reply.getUserId() != user.getId()) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        replyService.deleteReply(reply.getId());

        return "redirect:/" + targetType + "/detail/" + reply.getTargetId();
    }

}
