package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.File;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {
    // 리뷰아이디로 파일 리스트 조회
//    List<ReviewFile> findFilesByReviewId(Long id);
//
//    void insertPostFile(PostFile postFile);
//
//    List<PostFile> findByPostId(Long id);
//
//    void deleteFilesByPostId(Long id);
//
//    // 리뷰 파일 조회
//    ReviewFile getFileById(Long fileId);
//
//    // 리뷰 파일 삭제
//    void deleteFileById(Long fileId);
//
//    // 리뷰 파일 저장
//    void saveReviewFile(ReviewFile reviewFile);
//
//    // [수정됨] 통합 파일 저장
    void insertFile(File file);

    // [수정됨] 통합 파일 목록 조회
    List<File> findAllByTargetTypeAndTargetId(String targetType, Long targetId);

    // [수정됨] 통합 파일 단건 조회
//    FileAttachment findAttachmentById(Long id);
//
    // [수정됨] 통합 파일 개별 삭제 (이름 변경: deleteFileById -> deleteAttachment)
    void deleteFileById(Long id);

    // [수정됨] 통합 파일 전체 삭제 (글 삭제 시)
    void deleteFilesAllByTargetTypeAndTargetId(String targetType, Long targetId);

    // 파일 리스트 가져오기
    List<File> getFileListByTargetTypeAndTargetId(String targetType, Long targetId);
}