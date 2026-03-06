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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Reply updateReply(Long id, String content, Long currentUserId) {
        Reply reply = replyMapper.findById(id)
                .orElseThrow(() -> new NoSuchElementException("댓글이 없습니다."));

        if (!reply.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용이 비어있습니다.");
        }

        reply.setContent(content);
        replyMapper.updateReply(reply);

        return reply;
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

    public Page<ReplyDTO> findByTargetTypeAndUserId(String targetType, Long userId, Pageable pageable) {
        List<ReplyDTO> content = replyMapper.findByTargetTypeAndUserId(targetType, userId, pageable);
        int total = replyMapper.countByTargetTypeAndUserId(targetType, userId);
        return new PageImpl<>(content, pageable, total);
    }

    public List<ReplyDTO> getReplies(String targetType, Long targetId, int page, int size) {
        int offset = page * size;
        return replyMapper.findRepliesWithPaging(targetType, targetId, size, offset);
    }

    public Reply getReplyById(Long id) {
        return replyMapper.findById(id).orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다."));
    }
}
