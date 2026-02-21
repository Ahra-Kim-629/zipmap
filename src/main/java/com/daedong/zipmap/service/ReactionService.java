package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.mapper.ReactionMapper;
import com.daedong.zipmap.util.StatsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionMapper reactionMapper;
    private final StatsUtil statsUtil;

    @Transactional
    public void save(Reaction reaction) {
        // DB에서 반응 찾기
        Reaction existingLike = reactionMapper.findByUserAndTarget(reaction);

        if (existingLike == null) {
            // 있던 반응 없으면 저장
            reactionMapper.save(reaction);
            statsUtil.updateReactionCount(reaction.getTargetType(), reaction.getTargetId(), reaction.getType());
        } else {
            // 있던 반응 있으면
            if (existingLike.getType() == reaction.getType()) {
                // 리뷰의 반응이라면 다시 눌렀을때 '취소'처리
                reactionMapper.delete(existingLike.getId());
                statsUtil.updateReactionCount(reaction.getTargetType(), reaction.getTargetId(), -reaction.getType());
            } else if (reaction.getTargetType().equals("post")) {
                // 포스트의 반응이라면
                reactionMapper.update(existingLike.getId(), reaction.getType()); // DB 수정
                // 차이만큼 반영
                statsUtil.updateReactionCount("post", reaction.getTargetId(), reaction.getType() - existingLike.getType());
            }
        }
    }

    // 반응 개수 세기
    public long countReaction(String targetType, Long targetId, int type) {
        return reactionMapper.countReaction(targetType, targetId, type);
    }

    // 내가 누른 반응 표시
    public int getMyReaction(Reaction reaction) {
        Reaction existingLike = reactionMapper.findByUserAndTarget(reaction);
        return (existingLike != null) ? existingLike.getType() : 0;
    }
}
