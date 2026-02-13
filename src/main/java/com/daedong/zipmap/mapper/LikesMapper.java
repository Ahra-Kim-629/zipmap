package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Likes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikesMapper {
    Likes findByUserAndTarget(Likes like);

    void save(Likes like);

    void delete(Long id);

    void update(@Param("id") Long id, @Param("type") int type);

    int countLikes(@Param("targetType") String targetType,
                   @Param("targetId") Long targetId,
                   @Param("type") int type);
}
