package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.ReplyDTO;
import com.daedong.zipmap.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyMapper replyMapper;

    // 댓글 작성
    public void saveReply(Reply reply) {
        replyMapper.insertReply(reply);
    }

    // 댓글 수정
    public void updateReply(Reply reply) {
        replyMapper.updateReply(reply);
    }

    // 댓글 삭제
    public void deleteReply(Long id) {
        replyMapper.deleteReply(id);
    }

    // 댓글 리스트 조회
    public List<ReplyDTO> getReplyDTOList(String targetType, Long targetId) {
        return replyMapper.getRepliesByTargetTypeAndTargetId(targetType, targetId);
    }

    public void deleteByTargetTypeAndTargetId(String targetType, Long targetId) {
        replyMapper.deleteByTargetTypeAndTargetId(targetType, targetId);
    }

    // 내가 쓴 리뷰 댓글 조회
    public Page<ReplyDTO> findMyReplies(Long userId, Pageable pageable) {
        List<ReplyDTO> content = replyMapper.findByUserId(userId, pageable);
        int total = replyMapper.countByUserId(userId);
        return new PageImpl<>(content, pageable, total);
    }

    public Page<ReplyDTO> findByTargetTypeAndUserId(String targetType, Long userId, Pageable pageable) {
        List<ReplyDTO> content = replyMapper.findByTargetTypeAndUserId(targetType, userId, pageable);
        int total = replyMapper.countByTargetTypeAndUserId(targetType, userId);
        return new PageImpl<>(content, pageable, total);
    }
}
