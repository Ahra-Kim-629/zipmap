package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Reaction;
import com.daedong.zipmap.mapper.ReactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionMapper reactionMapper;

    public void save(Reaction reaction){
        // DB에서 반응 찾기
        Reaction existingLike = reactionMapper.findByUserAndTarget(reaction);

        if(existingLike == null) {
            // 있던 반응 없으면 저장
            reactionMapper.save(reaction);
        } else {
            // 있던 반응 있으면
            if(reaction.getTargetType().equals("review")) {
                // 리뷰의 반응이라면 다시 눌렀을때 '취소'처리
                reactionMapper.delete(existingLike.getId());
            } else if(reaction.getTargetType().equals("post")){
                // 포스트의 반응이라면
                if(existingLike.getType() == reaction.getType()) {
                    // 같은 버튼 또 누르면 '취소'처리
                    reactionMapper.delete(existingLike.getId());
                } else {
                    // 다른 버튼 누르면 변경 (싫어요)
                    reactionMapper.update(existingLike.getId(), reaction.getType());
                }
            }
        }
    }

    // 반응 개수 세기
    public int countLikes(String targetType, Long targetId, int type) {
        return reactionMapper.countLikes(targetType, targetId, type);
    }
}
