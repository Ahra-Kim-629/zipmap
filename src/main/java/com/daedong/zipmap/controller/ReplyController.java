package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.util.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

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
    public String delete(@RequestParam Long id,
                         @RequestParam String targetType,
                         @RequestParam Long targetId,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes rttr) {
        try {
            // 서비스에 댓글 ID와 현재 로그인한 유저의 ID를 함께 전달
            replyService.deleteReply(id, user.getId());
            rttr.addFlashAttribute("message", "댓글이 삭제되었습니다.");
        } catch (NoSuchElementException e) {
            rttr.addFlashAttribute("error", "이미 삭제된 댓글입니다.");
        } catch (AccessDeniedException e) {
            rttr.addFlashAttribute("error", "삭제 권한이 없습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }

        // 상세 페이지로 다시 리다이렉트
        return "redirect:/" + targetType + "/detail/" + targetId;
    }

}
