package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.File; // ★ 이제 파일 도메인은 이거 하나뿐입니다!
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {

    // [통합 파일 저장]
    void insertAttachment(File file);

    // [통합 파일 목록 조회]
    List<File> findAttachments(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    // [통합 파일 단건 조회]
    File findAttachmentById(Long id);

    // [통합 파일 개별 삭제]
    void deleteAttachment(Long id);

    // [통합 파일 전체 삭제 (글 삭제 시)]
    void deleteAttachmentsByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}