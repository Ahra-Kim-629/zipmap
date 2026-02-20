package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.Post;
import com.daedong.zipmap.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostStatsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostMapper postMapper; // MyBatis Mapper

    // Redis Key 설계
    private static final String KEY_STATS = "post:stats:";      // Hash: post:stats:{id} -> {viewCount, likeCount}
    private static final String KEY_RANKING = "post:ranking";   // Sorted Set: post:ranking -> {postId, score}

    /**
     * 1. 조회수 증가 (사용자 액션 시 호출)
     */
    public void updateViewCount(Long postId, String identifier) {
        // 새로고침으로 조회수 폭발 방지 로직 : identifier 는 ip or userId
        String viewLimitKey = "post:view:limit:" + postId + ":" + identifier;

        // 6시간 동안 키가 유지되도록 설정
        Boolean isFirstView = redisTemplate.opsForValue().setIfAbsent(viewLimitKey, "v", 6, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstView) || isFirstView == null) {
            return;
        }

        // Redis Hash에 카운트 누적
        redisTemplate.opsForHash().increment(KEY_STATS + postId, "viewCount", 1);
        redisTemplate.opsForZSet().incrementScore(KEY_RANKING, postId.toString(), 1);
    }

    /**
     * 2. 추천수 증감 (사용자 액션 시 호출)
     */
    public void updateReactionCount(Long postId, int delta) {
        // Redis Hash에 카운트 누적
        redisTemplate.opsForHash().increment(KEY_STATS + postId, "likeCount", delta);

        // Redis Sorted Set에 실시간 점수 반영 (메인페이지 노출용)
        double weight = 10.0 * delta;
        redisTemplate.opsForZSet().incrementScore(KEY_RANKING, postId.toString(), weight);
    }

    /**
     * 3. 메인페이지용 인기 게시글 ID 리스트 조회
     */
    public List<Long> getTopPostIds(int limit) {
        Set<String> ranking = redisTemplate.opsForZSet().reverseRange(KEY_RANKING, 0, limit - 1);

        // 만약 redis 가 비어있거나 데이터가 너무 적다면 즉시 DB 로딩 시도
        if (ranking == null || ranking.size() < 5) {
            refreshRankingFromDb();
            ranking = redisTemplate.opsForZSet().reverseRange(KEY_RANKING, 0, limit - 1);
        }

        return ranking.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * 4. [Write-Back] Redis 데이터를 DB(MyBatis)로 정기 동기화
     */
    @Scheduled(fixedDelay = 600000) // 10분마다 실행
    @Transactional
    public void syncRedisToDb() {
        // "post:stats:*" 패턴의 모든 키 조회
        Set<String> keys = redisTemplate.keys(KEY_STATS + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<Post> updateList = new ArrayList<>();

        for (String key : keys) {
            long id = Long.parseLong(key.replace(KEY_STATS, ""));

            // Hash 데이터 읽기 (null 처리를 위해 NumberUtils 또는 기본값 활용)
            Object likeObj = redisTemplate.opsForHash().get(key, "likeCount");
            Object viewObj = redisTemplate.opsForHash().get(key, "viewCount");

            int likeCount = (likeObj != null) ? Integer.parseInt(likeObj.toString()) : 0;
            int viewCount = (viewObj != null) ? Integer.parseInt(viewObj.toString()) : 0;

            Post post = new Post();
            post.setId(id);
            post.setLikeCount(likeCount);
            post.setViewCount(viewCount);
            updateList.add(post);

            // DB 반영 준비가 끝난 데이터는 Redis에서 삭제 (중복 반영 방지)
            redisTemplate.delete(key);
        }

        // MyBatis 벌크 업데이트 실행
        postMapper.updatePostStatsBatch(updateList);
    }

    /**
     * 5. DB -> Redis 랭킹 재구성 (랭킹 보정)
     * 매시간 정각에 실행하여 랭킹의 정확도를 높이고 '오래된 글'을 자연스럽게 밀어냄
     */
    @Scheduled(cron = "0 0 * * * *")
    public void refreshRankingFromDb() {
        // 최근 30일 이내 상위 100개 데이터 DB 조회
        List<Map<String, Object>> topPostList = postMapper.getTopPostList(30);

        if (topPostList == null) {
            return;
        }

        String tempKey = KEY_RANKING + ":temp";

        // 임시 ZSet에 데이터 빌드
        for (Map<String, Object> row : topPostList) {
            String postId = String.valueOf(row.get("id"));
            double score = ((Number) row.get("score")).doubleValue();
            redisTemplate.opsForZSet().add(tempKey, postId, score);
        }

        // 키 교체 (Rename 명령은 원자적이며 매우 빠름)
        redisTemplate.rename(tempKey, KEY_RANKING);
        // 랭킹 세트에만 유지 기간 설정 (예: 2시간 - 스케줄러가 실패해도 데이터가 너무 오래 남지 않도록)
        redisTemplate.expire(KEY_RANKING, 2, TimeUnit.HOURS);
    }
}