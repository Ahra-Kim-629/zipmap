package com.daedong.zipmap.postservice;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.postmapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostMapper postMapper;

    public Page<Post> findAll(String searchType, String keyword, Pageable pageable) {
        int totalCount = postMapper.countAll(searchType, keyword);
        List<Post> posts = postMapper.findAll(searchType, keyword, pageable);
        return new PageImpl<>(posts, pageable, totalCount);
    }
}
