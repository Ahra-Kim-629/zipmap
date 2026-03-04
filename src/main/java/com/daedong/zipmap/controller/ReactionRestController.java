package com.daedong.zipmap.controller;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.domain.UserPrincipalDetails;
import com.daedong.zipmap.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reaction")
public class ReactionRestController {
    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleReaction(String targetType, Long targetId, int type,
                                                              @AuthenticationPrincipal UserPrincipalDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 도메인(post, review), 대상ID, 타입(좋아요/싫어요)을 담은 DTO 전달
        Reaction reaction = new Reaction();
        reaction.setTargetType(targetType);
        reaction.setTargetId(targetId);
        reaction.setUserId(user.getUser().getId());
        reaction.setType(type);

        Map<String, Object> result = reactionService.save(reaction);

        return ResponseEntity.ok(result);
    }
}
