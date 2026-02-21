package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.StatsUpdateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {
    PostDTO findById(long id);

    List<Post> findAll(@Param("searchType") String searchType, @Param("keyword") String keyword);

    void save(Post post);

    int countAll(@Param("searchType") String searchType,
                 @Param("keyword") String keyword,
                 @Param("category") String category, // 추가
                 @Param("location") String location); // 추가

    long insertPost(Post post);

//    void insertFile(PostFile postFile);

    void updatePost(Post post);

    void deletePost(Long id);

    List<Post> adminFindAll(@Param("searchType") String searchType,
                            @Param("keyword") String keyword,
                            @Param("category") String category,
                            @Param("location") String location,
                            @Param("pageable") Pageable pageable);

    List<PostDTO> findAll(@Param("searchType") String searchType,
                          @Param("keyword") String keyword,
                          @Param("category") String category,
                          @Param("location") String location,
                          @Param("pageable") Pageable pageable);

    List<PostDTO> findAllByIdList(List<Long> postIdList);

    // UPDATE 커뮤니티 게시글 보이게 하기, 숨기게 하기 기능
    void updatePostStatus(@Param("id") Long id, @Param("status") String status);

    Integer findReaction(@Param("postId") Long postId, @Param("userId") String userId);

    void deleteReaction(@Param("postId") Long postId, @Param("userId") String userId);

    void updateBoardLikeCount(@Param("postId") Long postId);

    void insertReaction(@Param("postId") Long postId, @Param("userId") String userId, @Param("type") int type);

    // 내가 쓴 글 조회
    List<PostDTO> findByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

    int countByUserId(@Param("userId") Long userId);

    // 내가 쓴 댓글 조회
//    List<PostReply> findRepliesByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);
//    int countRepliesByUserId(@Param("userId") Long userId);
//
//    PostReplyDTO findReplyById(Long id);
//
//    void deleteReply(Long id);
//
//    void insertReply(PostReply reply);
//
//    void updateReply(PostReply reply);
//
//    List<PostDTO> findMainpagePost();

    void updateContent(Post post);

    void updatePostStatsBatch(List<StatsUpdateDTO> syncList);

    List<Map<String, Object>> getTopPostList(int i);
}
