package com.daedong.zipmap.service;

import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostReplyService {
    private final PostMapper postMapper;

//    public void saveReply(PostReply reply) {
//        postMapper.insertReply(reply);
//    }

//    public void deleteReply(Long id) {
//        postMapper.deleteReply(id);
//    }
//
//    public void updateReply(PostReply reply) {
//        postMapper.updateReply(reply);
//    }
// 수정: id 외에 현재 로그인한 사용자 정보와 관리자 여부를 전달받음
//public void deleteReply(Long id, String currentUserId, boolean isAdmin) {
//    // 1. DTO로 변경하여 가져옵니다.
//    PostReplyDTO reply = postMapper.findReplyById(id);
//
//    if (reply == null) {
//        throw new RuntimeException("해당 댓글이 존재하지 않습니다.");
//    }
//
//    // 2. 이제 loginId(문자열)끼리 비교하므로 bbb == bbb 가 성립합니다!
//    if (reply.getLoginId().equals(currentUserId) || isAdmin) {
//        postMapper.deleteReply(id);
//    } else {
//        throw new RuntimeException("삭제 권한이 없습니다.");
//    }
//}
//
//    public void updateReply(PostReply reply) {
//        postMapper.updateReply(reply);
//    }
//
}