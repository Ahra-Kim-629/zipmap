package com.daedong.zipmap.domain.interaction.reply.mapper;

import com.daedong.zipmap.domain.interaction.reply.entity.Reply;
import com.daedong.zipmap.domain.interaction.reply.dto.ReplyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ReplyMapper {
    void insertReply(Reply reply);

    void updateReply(Reply reply);

    Optional<Reply> findById(Long id);

    void deleteReply(Long id);

    List<ReplyDTO> findRepliesByTargetTypeAndTargetId(String targetType, Long targetId);

    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);

    List<ReplyDTO> findByTargetTypeAndUserId(String targetType, Long userId, Pageable pageable);

    int countByTargetTypeAndUserId(String targetType, Long userId);

    List<ReplyDTO> findRepliesWithPaging(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
