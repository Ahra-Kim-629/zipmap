package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.ReplyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface ReplyMapper {
    void insertReply(Reply reply);

    void updateReply(Reply reply);

    void deleteReply(Long id);

    List<ReplyDTO> getRepliesByTargetTypeAndTargetId(String targetType, Long targetId);

    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);

    List<ReplyDTO> findByUserId(Long userId, Pageable pageable);

    int countByUserId(Long userId);

    List<ReplyDTO> findByTargetTypeAndUserId(String targetType, Long userId, Pageable pageable);

    int countByTargetTypeAndUserId(String targetType, Long userId);
}
