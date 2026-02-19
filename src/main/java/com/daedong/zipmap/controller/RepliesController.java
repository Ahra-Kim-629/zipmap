package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.util.RepliesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reply")
@RequiredArgsConstructor
public class RepliesController {
    private final RepliesService repliesService;

    // 댓글 작성
    @PostMapping("/write")
    public String write(Reply reply, @AuthenticationPrincipal User user) {
        reply.setUserId(user.getId());
        reply.setLoginId(user.getLoginId());

        repliesService.saveReply(reply);

        return "redirect:/" + reply.getTargetType() + "/detail/" + reply.getTargetId();
    }

    // 댓글 수정
    @PostMapping("/edit")
    public String edit(Reply reply) {
        repliesService.updateReply(reply);

        return "redirect:/" + reply.getTargetType() + "/detail/" + reply.getTargetId();
    }

    // 댓글 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam("replyId") Long replyId, Reply reply, @AuthenticationPrincipal User user){
        if (reply.getUserId() != user.getId()) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        repliesService.deleteReply(replyId);

        return "redirect:/" + reply.getTargetType() + "/detail/" + reply.getTargetId();
    }

}
