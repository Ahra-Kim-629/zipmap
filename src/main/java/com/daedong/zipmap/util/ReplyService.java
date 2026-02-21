package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.ReplyDTO;
import com.daedong.zipmap.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
    public void deleteReply(Long replyId, Long currentUserId) {
        Reply reply = replyMapper.findById(replyId).orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다."));

        if (!reply.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        replyMapper.deleteReply(replyId);
    }

    // 댓글 리스트 조회
    public List<ReplyDTO> getReplyDTOList(String targetType, Long targetId) {
        return replyMapper.findRepliesByTargetTypeAndTargetId(targetType, targetId);
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
