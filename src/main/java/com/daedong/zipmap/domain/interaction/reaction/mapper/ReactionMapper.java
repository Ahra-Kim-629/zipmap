package com.daedong.zipmap.domain.interaction.reaction.mapper;

import com.daedong.zipmap.domain.post.dto.PostDTO;
import com.daedong.zipmap.domain.interaction.reaction.entity.Reaction;
import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface ReactionMapper {
    Reaction findByUserAndTarget(Reaction like);

    void save(Reaction like);

    void delete(Long id);

    void update(@Param("id") Long id, @Param("type") int type);

    int countReaction(@Param("targetType") String targetType,
                   @Param("targetId") Long targetId,
                   @Param("type") int type);

    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);

    List<ReviewDTO> findLikedReviewsByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

    int countLikedReviewsByUserId(@Param("userId") Long userId);

    List<PostDTO> findLikedPostsByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

    int countLikedPostsByUserId(@Param("userId") Long userId);

}
