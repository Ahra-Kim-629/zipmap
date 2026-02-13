package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Likes;
import com.daedong.zipmap.mapper.LikesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikesService {
    private final LikesMapper likesMapper;

    public void save(Likes like){
        // DB에서 반응 찾기
        Likes existingLike = likesMapper.findByUserAndTarget(like);

        if(existingLike == null) {
            // 있던 반응 없으면 저장
            likesMapper.save(like);
        } else {
            // 있던 반응 있으면
            if(like.getTargetType().equals("review")) {
                // 리뷰의 반응이라면 다시 눌렀을때 '취소'처리
                likesMapper.delete(existingLike.getId());
            } else if(like.getTargetType().equals("post")){
                // 포스트의 반응이라면
                if(existingLike.getType() == like.getType()) {
                    // 같은 버튼 또 누르면 '취소'처리
                    likesMapper.delete(existingLike.getId());
                } else {
                    // 다른 버튼 누르면 변경 (싫어요)
                    likesMapper.update(existingLike.getId(), like.getType());
                }
            }
        }
    }

    // 반응 개수 세기
    public int countLikes(String targetType, Long targetId, int type) {
        return likesMapper.countLikes(targetType, targetId, type);
    }
}
