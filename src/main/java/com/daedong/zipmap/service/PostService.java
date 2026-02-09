package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;

    public List<Post> findAll(String searchType, String key) {
        return postMapper.findAll(searchType, key);
    }

    public PostDTO getPostDetail(Long id) {
        return postMapper.findById(id);
    }
}
