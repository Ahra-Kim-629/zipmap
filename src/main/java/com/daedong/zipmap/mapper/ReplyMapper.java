package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Reply;
import com.daedong.zipmap.domain.ReplyDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReplyMapper {
    void insertReply(Reply reply);

    void updateReply(Reply reply);

    void deleteReply(Long id);

    List<ReplyDTO> getRepliesByTargetTypeAndTargetId(String targetType, Long targetId);
}
