package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    //   private final FileService fileService;

//    public Page<PostDTO> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
//        int totalCount = postMapper.countAll(searchType, keyword, category, location);
////        List<Post> posts = postMapper.findAll(searchType, keyword, pageable);
////        return new PageImpl<>(posts, pageable, totalCount);
//        List<PostDTO> posts = postMapper.findAll(searchType, keyword, category, location, pageable);
//        return new PageImpl<PostDTO>(posts, pageable, totalCount);
//    }
//
//    public PostDTO getPostDetail(Long id) {
//        return postMapper.findById(id);
//    }

    @Transactional(rollbackFor = Exception.class)
    public Long write(Post post) { // 리턴타입 void -> Long (ID 반환)
        postMapper.insertPost(post);
        return post.getId(); // XML에서 keyProperty="id"로 세팅된 ID 반환
    }

    // [수정됨] 파일 처리 로직 제거
    @Transactional(rollbackFor = Exception.class)
    public void update(Post post) {
        postMapper.updatePost(post);
    }

    // [추가] 내용(HTML)만 업데이트하는 메서드 (필수!)
    @Transactional
    public void updateContent(Long id, String content) {
        // Post 객체를 만들어서 Mapper에 전달
        Post post = new Post();
        post.setId(id);
        post.setContent(content);
        postMapper.updateContent(post); // Mapper XML에 추가한 쿼리 호출
    }

    @Transactional
    public void delete(Long id) {
        // 기존 파일 삭제 로직 제거 (Controller에서 FileUtilService로 처리)
        postMapper.deletePost(id);
    }
    // 좋아요 기능 관련 추가
    @Transactional
    public void saveOrUpdateReaction(Long postId, String userId, String typeStr) {
        int newType = typeStr.equals("LIKE") ? 1 : 2;
        Integer existingType = postMapper.findReaction(postId, userId);

        if (existingType != null) {
            if (existingType == newType) {
                // 이미 같은 걸 눌렀다면? -> 반응 삭제 (취소)
                postMapper.deleteReaction(postId, userId);
            } else {
                // 다른 걸 눌렀다면? (좋아요 누른 상태에서 싫어요 클릭) -> 타입 변경
                // insertReaction에 ON DUPLICATE KEY UPDATE가 구현되어 있어야 합니다.
                postMapper.insertReaction(postId, userId, newType);
            }
        } else {
            // 처음 누르는 거라면? -> 데이터 추가
            postMapper.insertReaction(postId, userId, newType);
        }

        // 3. 최종 결과를 게시글 테이블(post)에 반영하여 숫자를 맞춤
        postMapper.updateBoardLikeCount(postId);
    }

    public Page<Post> findMyPosts(Long userId, Pageable pageable) {
        int totalCount = postMapper.countByUserId(userId);
        List<Post> posts = postMapper.findByUserId(userId, pageable);
        return new PageImpl<>(posts, pageable, totalCount);
    }

//    public Page<PostReply> findMyReplies(Long userId, Pageable pageable) {
//        int totalCount = postMapper.countRepliesByUserId(userId);
//        List<PostReply> replies = postMapper.findRepliesByUserId(userId, pageable);
//        return new PageImpl<>(replies, pageable, totalCount);
//    }
//
//    @Cacheable(value = "mainPostList")
//    public List<PostDTO> getMainpagePost() {
//        return postMapper.findMainpagePost();
//    }
}
