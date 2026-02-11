package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.PostFile;
import com.daedong.zipmap.domain.ReviewFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {

    List<ReviewFile> findFilesByReviewId(Long id);

    void insertPostFile(PostFile postFile);

    List<PostFile> findByPostId(Long id);

    List<ReviewFile> findByReviewId(Long id);

    void deleteFilesByPostId(Long id);
}
