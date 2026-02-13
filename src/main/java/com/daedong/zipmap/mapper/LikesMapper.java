package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Likes;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikesMapper {
    Likes findByUserAndTarget(Likes like);

    void save(Likes like);

    void delete(Long id);

    void update(Long id, int type);

    int countLikes(String targetType, Long targetId, int type);
}
