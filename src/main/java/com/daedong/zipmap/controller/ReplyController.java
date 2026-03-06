package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.ReplyDTO;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.util.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/write")
    public String write(Reply reply,
                        @AuthenticationPrincipal UserPrincipalDetails user,
                        RedirectAttributes rttr) {

        // 1. 유저 정보 체크
        if (user == null || user.getUser() == null) {
            rttr.addFlashAttribute("error", "로그인 세션이 만료되었습니다.");
            return "redirect:/login";
        }

        // [수정] 대문자 강제 변환 로직 삭제
        // 이유: post는 소문자로 저장되는데 review만 대문자로 저장되어 조회 시 불일치 발생
        // 클라이언트에서 보내준 값(소문자) 그대로 저장합니다.
        /*
        if (reply.getTargetType() != null) {
            reply.setTargetType(reply.getTargetType().toUpperCase());
        }
        */

        reply.setUserId(user.getUser().getId());
        replyService.saveReply(reply);

        // 3. 리다이렉트: 성공 시 해당 상세 페이지로 다시 보냄
        return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();
    }

    @PostMapping("/edit")
    public String edit(Long id, String content,
                       @AuthenticationPrincipal UserPrincipalDetails user, RedirectAttributes rttr) {
        try {
            // 수정 성공 시 반환된 reply 객체 활용
            Reply reply = replyService.updateReply(id, content, user.getUser().getId());
            rttr.addFlashAttribute("message", "수정되었습니다.");
            return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();

        } catch (AccessDeniedException | IllegalArgumentException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            // 예외 발생 시에도 안전하게 해당 게시물로 돌아가기 위해 다시 조회
            Reply reply = replyService.getReplyById(id);
            return "redirect:/" + reply.getTargetType().toLowerCase() + "/detail/" + reply.getTargetId();

        } catch (NoSuchElementException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // 댓글 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         @RequestParam String targetType,
                         @RequestParam Long targetId,
                         @AuthenticationPrincipal UserPrincipalDetails user,
                         RedirectAttributes rttr) {

        try {
            // 서비스에 댓글 ID와 현재 로그인한 유저의 ID를 함께 전달
            replyService.deleteReply(id, user.getUser().getId());
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

    @GetMapping("/list") // 전체 주소: /reply/list
    @ResponseBody        // 중요: HTML 페이지가 아닌 '데이터'만 보낸다는 뜻
    public List<ReplyDTO> getReplyList(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page) {

        int size = 10;
        return replyService.getReplies(targetType, targetId, page, size);
    }

}
