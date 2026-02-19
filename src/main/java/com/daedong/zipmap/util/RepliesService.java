package com.daedong.zipmap.util;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepliesService {
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
    public List<Reply> getReplyList(String targetType, Long targetId) {
        return replyMapper.getRepliesByTarget(targetType, targetId);
    }

//    public Reply findReplyById(Long replyId) {
//        return replyMapper.findReplyById(replyId);
//    }
}
