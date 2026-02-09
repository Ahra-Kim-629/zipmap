package com.daedong.zipmap.postservice;

import com.daedong.zipmap.postdomain.Post;
import com.daedong.zipmap.postmapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostMapper postMapper;

    public List<Post> findAll() {

        return postMapper.findAll();
    }
}
