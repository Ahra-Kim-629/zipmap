package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.domain.PostDTO;
import com.daedong.zipmap.domain.Status;
import com.daedong.zipmap.mapper.PostMapper;
import com.daedong.zipmap.util.FileUtilService;
import com.daedong.zipmap.util.NetworkUtil;
import com.daedong.zipmap.util.ReplyService;
import com.daedong.zipmap.util.StatsUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final StatsUtil statsUtil;
    private final FileUtilService fileUtilService;
    private final ReplyService replyService;
    private final ReactionService reactionService;
    private final AlarmService alarmService;

    public Page<PostDTO> findAll(String searchType, String keyword, String category, String location, Pageable pageable) {
        int totalCount = postMapper.countAll(searchType, keyword, category, location);
        List<PostDTO> posts = postMapper.findAll(searchType, keyword, category, location, pageable);
        return new PageImpl<PostDTO>(posts, pageable, totalCount);
    }

    @Transactional
    public PostDTO getPostDetail(Long id, HttpServletRequest request, UserDetails userDetails) {
        PostDTO postDTO = postMapper.findById(id);

        // 조회수 증가 위한 redis 처리
        if (postDTO != null) {

            // 로그인 했으면 userId 를, 안했으면 IP 를 식별자로 보내줌
            String identifier = (userDetails != null) ? userDetails.getUsername() : NetworkUtil.getClientIp(request);

            // 예외 처리를 추가하여 Redis 장애가 게시글 조회를 막지 않도록 보호
            try {
                statsUtil.updateViewCount("post", id, identifier);
            } catch (Exception e) {
                System.out.println("Redis 카운트 증가 실패 (게시글 ID: {" + id + "}): {" + e.getMessage() + "}");
            }
        }

        // 좋아요 표시
        postDTO.setLikeCount(reactionService.countReaction("post", id, 1));
        postDTO.setDislikeCount(reactionService.countReaction("post", id, -1));

        // 파일 리스트 추가
        postDTO.setFileList(fileUtilService.getFileList("post", id));

        // 댓글 리스트 추가
        postDTO.setReplyList(replyService.getReplyDTOList("post", id));

        return postDTO;
    }

    public PostDTO getPostDetail(long id) {
        return postMapper.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long write(Post post) { // 리턴타입 void -> Long (ID 반환)

        //  게시글 작성 시 기본 상태를 'ACTIVE(정상)'로 강력하게 고정
        post.setPostStatus(Status.ACTIVE);

        postMapper.insertPost(post);

        // 알림 서비스에게 글 정보 전달
        alarmService.sendPostAlarm(post);

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
        // ★ 파일 전체 삭제 (DB + 실제파일)
        fileUtilService.deleteFilesByTargetTypeAndTargetId("post", id);
        // 리플 삭제
        replyService.deleteByTargetTypeAndTargetId("post", id);
        // 리액션 삭제
        reactionService.deleteByTargetTypeAndTargetId("post", id);
        // 게시글 본체 삭제
        postMapper.deletePost(id);
    }

    public Page<PostDTO> getMyPosts(Long userId, Pageable pageable) {
        List<PostDTO> posts = postMapper.findByUserId(userId, pageable);
        int total = postMapper.countByUserId(userId);
        return new PageImpl<>(posts, pageable, total);
    }

    @Cacheable(value = "mainPostList")
    public List<PostDTO> getMainpagePost() {
        List<Long> topPostIdList = statsUtil.getTopPostIds(5);

        if (topPostIdList == null || topPostIdList.isEmpty()) {
            return new ArrayList<>(); // 빈 리스트 반환하여 쿼리 실행 방지
        }

        return postMapper.findAllByIdList(topPostIdList);
    }

    // [추가] 선택된 ID 목록으로 게시글 조회
    public List<PostDTO> getPostsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return postMapper.findAllByIdList(ids);
    }
    //관리자용 전체 게시글 조회
    public Page<Post> findAllAdmin(String searchType, String keyword, String category, String location, Pageable pageable) {
        List<Post> posts = postMapper.adminFindAll(searchType, keyword, category, location, pageable);
        int totalCount = postMapper.countAll(searchType, keyword, category, location);
        return new PageImpl<>(posts, pageable, totalCount);
    }

    // 게시글 상태 토글 (ACTIVE <-> BANNED)
    // 이 로직도 Post와 관련된 관심사이므로 PostService에 있는 것이 자연스러워 가져옴.
    @Transactional
    public void togglePostStatus(Long id, String currentStatus) {
        Status newStatus = "ACTIVE".equals(currentStatus) ? Status.BANNED : Status.ACTIVE;
        postMapper.updatePostStatus(id, newStatus);
    }
}