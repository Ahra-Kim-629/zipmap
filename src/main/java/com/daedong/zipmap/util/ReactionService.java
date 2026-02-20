package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.mapper.ReactionMapper;
import com.daedong.zipmap.service.PostStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionMapper reactionMapper;
    private final PostStatsService postStatsService;

    @Transactional
    public void save(Reaction reaction) {
        // DB에서 반응 찾기
        Reaction existingLike = reactionMapper.findByUserAndTarget(reaction);

        if (existingLike == null) {
            // 있던 반응 없으면 저장
            reactionMapper.save(reaction);
            if (reaction.getTargetType().equals("post")) {
                postStatsService.updateReactionCount(reaction.getTargetId(), reaction.getType());
            }
        } else {
            // 있던 반응 있으면
            if (reaction.getTargetType().equals("review")) {
                // 리뷰의 반응이라면 다시 눌렀을때 '취소'처리
                reactionMapper.delete(existingLike.getId());
            } else if (reaction.getTargetType().equals("post")) {
                // 포스트의 반응이라면
                if (existingLike.getType() == reaction.getType()) {
                    // 같은 버튼 또 누르면 '취소'처리
                    reactionMapper.delete(existingLike.getId());
                    postStatsService.updateReactionCount(reaction.getTargetId(), -reaction.getType());
                } else {
                    // 다른 버튼 누르면 변경 (싫어요)
                    reactionMapper.update(existingLike.getId(), reaction.getType());
                    postStatsService.updateReactionCount(reaction.getTargetId(), reaction.getType() - existingLike.getType());
                }
            }
        }
    }

    // 반응 개수 세기
    public int countReaction(String targetType, Long targetId, int type) {
        return reactionMapper.countReaction(targetType, targetId, type);
    }

    // 내가 누른 반응 표시
    public int getMyReaction(Reaction reaction) {
        Reaction existingLike = reactionMapper.findByUserAndTarget(reaction);
        return (existingLike != null) ? existingLike.getType() : 0;
    }
}
