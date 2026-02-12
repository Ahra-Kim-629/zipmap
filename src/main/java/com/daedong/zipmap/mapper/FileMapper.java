package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.PostFile;
import com.daedong.zipmap.domain.ReviewFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {
    // 리뷰아이디로 파일 리스트 조회
    List<ReviewFile> findFilesByReviewId(Long id);

    void insertPostFile(PostFile postFile);

    List<PostFile> findByPostId(Long id);

    void deleteFilesByPostId(Long id);

    // 리뷰 파일 조회
    ReviewFile getFileById(Long fileId);

    // 리뷰 파일 삭제
    void deleteFileById(Long fileId);

    // 리뷰 파일 저장
    void saveReviewFile(ReviewFile reviewFile);
}
