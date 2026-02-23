package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.domain.ReviewDTO;
import com.daedong.zipmap.mapper.ReactionMapper;
import com.daedong.zipmap.util.StatsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public void deleteByTargetTypeAndTargetId(String targetType, Long targetId) {
        reactionMapper.deleteByTargetTypeAndTargetId(targetType, targetId);
    }

    public Page<ReviewDTO> getLikedReviews(Long userId, Pageable pageable) {
        List<ReviewDTO> content = reactionMapper.findLikedReviewsByUserId(userId, pageable);
        int total = reactionMapper.countLikedReviewsByUserId(userId);
        return new PageImpl<>(content, pageable, total);
    }

    public Page<PostDTO> getLikedPosts(Long userId, Pageable pageable) {
        List<PostDTO> content = reactionMapper.findLikedPostsByUserId(userId, pageable);
        int total = reactionMapper.countLikedPostsByUserId(userId);
        return new PageImpl<>(content, pageable, total);
    }
}
