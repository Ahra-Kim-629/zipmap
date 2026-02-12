package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Replies;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RepliesMapper {
    void insertReply(Replies reply);

    void updateReply(Replies reply);

    void deleteReply(Long id);

    List<Replies> getRepliesByTarget(String targetType, Long targetId);
}
