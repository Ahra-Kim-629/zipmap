package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.File;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {
    void insertFile(File file);

    List<File> findAllByTargetTypeAndTargetId(String targetType, Long targetId);

    void deleteFileById(Long id);

    void deleteFilesAllByTargetTypeAndTargetId(String targetType, Long targetId);

    List<File> findFileListByTargetTypeAndTargetId(String targetType, Long targetId);
}