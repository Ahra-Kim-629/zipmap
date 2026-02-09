package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.repository.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;

    public PostDTO getPostDetail(Long id) {
        postMapper.findById(id);
        return null;
    }
}
