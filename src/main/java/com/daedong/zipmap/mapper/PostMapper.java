package com.daedong.zipmap.mapper;

import com.daedong.zipmap.domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface PostMapper {
    PostDTO findById(long id);

    List<Post> findAll(String searchType, String key);

    void save(Post post);

    List<Post> findAll(@Param("searchType") String searchType,
                       @Param("keyword") String keyword,
                       @Param("category") String category, // 추가
                       @Param("location") String location, // 추가
                       @Param("pageable") Pageable pageable);


    int countAll(@Param("searchType") String searchType,
                 @Param("keyword") String keyword,
                 @Param("category") String category, // 추가
                 @Param("location") String location); // 추가

    long insertPost(Post post);

    void insertFile(PostFile postFile);

    PostReplyDTO findReplyById(Long id);
    // 개시글 댓글 기능 관련 추가
    void insertReply(PostReply reply);

    // 개시글 삭제 기능 추가
    void deleteReply(Long id);

    // 개시글 수정 기능 추가
    void updateReply(PostReply reply);

    // 게시글 댓글 삭제 기능



    void updatePost(Post post);

    void deletePost(Long id);
}
