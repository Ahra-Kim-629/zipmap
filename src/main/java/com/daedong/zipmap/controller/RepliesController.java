package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.UserService;
import com.daedong.zipmap.util.RepliesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reply")
@RequiredArgsConstructor
public class RepliesController {
    private final RepliesService repliesService;
    private final UserService userService;


    // 댓글 작성
    @PostMapping("/write")
    public String write(@RequestParam("targetType") String targetType, @RequestParam("targetId") Long targetId, Reply reply, @AuthenticationPrincipal User user) {
        reply.setTargetType(targetType);
        reply.setTargetId(targetId);
        reply.setUserId(user.getId());

        repliesService.saveReply(reply);

        return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();
    }

    // 댓글 수정
    @PostMapping("/edit")
    public String edit(Reply reply) {
        repliesService.updateReply(reply);

        return "redirect:/" + reply.getTargetType() + "/detail/" + reply.getTargetId();
    }

    // 댓글 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam("replyId") Long replyId, @AuthenticationPrincipal User user){
        Reply reply = repliesService.findReplyById(replyId);

        // 댓글이 존재하는지, 작성자 정보가 있는지 확인
        if (reply == null || reply.getUserId() == null) {
            throw new RuntimeException("댓글 정보를 찾을 수 없습니다.");
        }

        // 권한 확인 (작성자와 로그인한 유저가 같은지)
        if (!reply.getUserId().equals(user.getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        repliesService.deleteReply(replyId);

        return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();
    }

}
