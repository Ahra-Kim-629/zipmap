package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;

    public Page<Post> findAll(String searchType, String keyword, Pageable pageable) {
        int totalCount = postMapper.countAll(searchType, keyword);
        List<Post> posts = postMapper.findAll(searchType, keyword, pageable);
        return new PageImpl<>(posts, pageable, totalCount);
    }

    public PostDTO getPostDetail(Long id) {
        return postMapper.findById(id);
    }

    public void write(Post post, MultipartFile file) {
        postMapper.write(post, file);
    }
}
