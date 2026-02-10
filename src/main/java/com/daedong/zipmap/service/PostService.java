package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;

    public Page<Post> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
        int totalCount = postMapper.countAll(searchType, keyword, category, location);
//        List<Post> posts = postMapper.findAll(searchType, keyword, pageable);
//        return new PageImpl<>(posts, pageable, totalCount);
        List<Post> posts = postMapper.findAll(searchType, keyword, category, location, pageable);
        return new PageImpl<>(posts, pageable, totalCount);
    }


    public PostDTO getPostDetail(Long id) {
        return postMapper.findById(id);
    }
}
