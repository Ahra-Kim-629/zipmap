package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.PostFile;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    public void write(Post post, List<MultipartFile> files) throws IOException {
        postMapper.insertPost(post);

        if (files != null && !files.isEmpty()) {
            String uploadDir = "D:/SaDang/first_project/files";

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    File saveFile = new File(uploadDir, fileName);
                    file.transferTo(saveFile);

                    PostFile postFile = new PostFile();
                    postFile.setPostId(post.getId());
                    postFile.setFilePath(fileName);
                    postMapper.insertFile(postFile);
                }
            }

        }
    }
}
