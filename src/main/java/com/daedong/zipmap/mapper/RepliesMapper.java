package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Reply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RepliesMapper {
    void insertReply(Reply reply);

    void updateReply(Reply reply);

    void deleteReply(Long id);

    List<Reply> getRepliesByTarget(String targetType, Long targetId);
}
