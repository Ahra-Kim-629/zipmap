package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final FileService fileService;

    public Page<PostDTO> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
        int totalCount = postMapper.countAll(searchType, keyword, category, location);
//        List<Post> posts = postMapper.findAll(searchType, keyword, pageable);
//        return new PageImpl<>(posts, pageable, totalCount);
        List<PostDTO> posts = postMapper.findAll(searchType, keyword, category, location, pageable);
        return new PageImpl<PostDTO>(posts, pageable, totalCount);
    }

    public PostDTO getPostDetail(Long id) {
        return postMapper.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void write(Post post, List<MultipartFile> files) throws IOException {
        postMapper.insertPost(post);

        fileService.saveFiles(post.getId(), files);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Post post, List<MultipartFile> files) throws IOException {
        postMapper.updatePost(post);

        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            fileService.deleteFilesByPostId(post.getId());
            fileService.saveFiles(post.getId(), files);
        }
    }

    @Transactional
    public void delete(Long id) {
        fileService.deleteFilesByPostId(id);
        postMapper.deletePost(id);
    }
}
