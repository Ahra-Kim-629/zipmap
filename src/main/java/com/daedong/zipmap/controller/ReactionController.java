package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.domain.User;
import com.daedong.zipmap.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reaction")
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping
    public String toggleReaction(String targetType, Long targetId, int type,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes rttr) {

        if (user == null) {
            rttr.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/" + targetType + "/detail/" + targetId;
        }

        // 도메인(post, review), 대상ID, 타입(좋아요/싫어요)을 담은 DTO 전달
        Reaction reaction = new Reaction();
        reaction.setTargetType(targetType);
        reaction.setTargetId(targetId);
        reaction.setUserId(user.getId());
        reaction.setType(type);

        reactionService.save(reaction);

        return "redirect:/" + targetType + "/detail/" + targetId;
    }
}
