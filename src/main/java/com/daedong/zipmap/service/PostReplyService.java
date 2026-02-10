package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostReply;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostReplyService {
    private final PostMapper postMapper;

    public void saveReply(PostReply reply) {
        // 매퍼의 insertReply 쿼리를 실행합니다.
        postMapper.insertReply(reply);
    }
}