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
        postMapper.insertReply(reply);
    }

    public void deleteReply(Long id) {
        postMapper.deleteReply(id);
    }

    public void updateReply(PostReply reply) {
        postMapper.updateReply(reply);
    }
}