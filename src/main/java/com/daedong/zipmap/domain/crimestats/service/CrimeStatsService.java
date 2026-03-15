package com.daedong.zipmap.domain.crimestats.service;

import com.daedong.zipmap.domain.review.dto.ReviewDTO;
import com.daedong.zipmap.domain.crimestats.entity.CrimeStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * 범죄 통계 서비스
 * - 공공데이터포털 API 연동
 * - 서울시 구별 범죄 데이터 집계 및 등급 산정 (상대평가)
 */
@Slf4j
@Service
public class CrimeStatsService {

    // application-secret.properties에서 API 접속 정보 가져오기
    @Value("${public.data.crime.url:}")
    private String crimeApiUrl;

    @Value("${public.data.crime.key:}")
    private String crimeApiKey;

    // API 호출 결과를 메모리에 저장해둘 리스트 (DB 대용)
    private List<CrimeStats> cachedData = new ArrayList<>();

    // 서울 25개 자치구의 위도/경도 좌표 (API에서 좌표를 안 줘서 직접 입력)
    // 카카오 map api 에서 주소 요청을 해서 받을수 있지만
    // 리뷰같은 경우에는 사용자 주소가 매번 바껴서 map에 요청하는게 맞음
    // 하지만 이건 25개 구가 변하지 않고 고정이기 때문에 등록하고 쓰는게 효율적
    private static final Map<String, double[]> SEOUL_COORDS = new HashMap<>();
    static {
        SEOUL_COORDS.put("강남구", new double[]{37.5172, 127.0473});
        SEOUL_COORDS.put("강동구", new double[]{37.5301, 127.1238});
        SEOUL_COORDS.put("강북구", new double[]{37.6396, 127.0257});
        SEOUL_COORDS.put("강서구", new double[]{37.5509, 126.8497});
        SEOUL_COORDS.put("관악구", new double[]{37.4784, 126.9516});
        SEOUL_COORDS.put("광진구", new double[]{37.5385, 127.0822});
        SEOUL_COORDS.put("구로구", new double[]{37.4954, 126.8874});
        SEOUL_COORDS.put("금천구", new double[]{37.4565, 126.8954});
        SEOUL_COORDS.put("노원구", new double[]{37.6542, 127.0568});
        SEOUL_COORDS.put("도봉구", new double[]{37.6688, 127.0471});
        SEOUL_COORDS.put("동대문구", new double[]{37.5744, 127.0400});
        SEOUL_COORDS.put("동작구", new double[]{37.5124, 126.9393});
        SEOUL_COORDS.put("마포구", new double[]{37.5665, 126.9018});
        SEOUL_COORDS.put("서대문구", new double[]{37.5791, 126.9368});
        SEOUL_COORDS.put("서초구", new double[]{37.4837, 127.0324});
        SEOUL_COORDS.put("성동구", new double[]{37.5633, 127.0371});
        SEOUL_COORDS.put("성북구", new double[]{37.5891, 127.0182});
        SEOUL_COORDS.put("송파구", new double[]{37.5145, 127.1066});
        SEOUL_COORDS.put("양천구", new double[]{37.5169, 126.8660});
        SEOUL_COORDS.put("영등포구", new double[]{37.5263, 126.8962});
        SEOUL_COORDS.put("용산구", new double[]{37.5326, 126.9900});
        SEOUL_COORDS.put("은평구", new double[]{37.6027, 126.9291});
        SEOUL_COORDS.put("종로구", new double[]{37.5729, 126.9796});
        SEOUL_COORDS.put("중구", new double[]{37.5641, 126.9970});
        SEOUL_COORDS.put("중랑구", new double[]{37.6063, 127.0926});
    }

    /**
     * 서버 실행 시 최초 1회 자동 실행
     * 데이터를 미리 가져와서 cachedData에 저장함
     */
    @PostConstruct
    public void init() {
        log.info("============== [안전율 데이터 로딩 시작] ==============");
        loadCrimeData();
        log.info("============== [안전율 데이터 로딩 종료] ==============");
    }

    /**
     * Controller에서 호출하는 메서드
     * 메모리에 저장된 데이터를 반환 (데이터가 없으면 다시 로딩 시도)
     */
    public List<CrimeStats> getSafetyData() {
        if (cachedData.isEmpty()) {
            loadCrimeData();
        }
        return cachedData;
    }

    /**
     * 실제 비즈니스 로직: API 호출 -> 파싱 -> 집계 -> 등급 산정
     */
    private void loadCrimeData() {
        try {
            // 1. 공공데이터 API 호출하여 JSON 문자열 받아오기
            String jsonString = callApi();
            if (jsonString == null) return;

            // 2. JSON 파싱하여 구별 범죄 건수 집계 (Map<구이름, 총건수>)
            Map<String, Integer> crimeCountMap = parseAndAggregate(jsonString);

            // 3. Map을 List<CrimeStat>으로 변환 (좌표 정보 추가)
            List<CrimeStats> statList = convertToList(crimeCountMap);

            // 4. 범죄 건수가 적은 순서대로 정렬 (오름차순: 적을수록 1등)
            statList.sort(Comparator.comparingInt(CrimeStats::getCrimeCount));

            // 5. 등수별 등급 부여 (상대평가)
            assignGradeByRank(statList);

            // 6. 결과 저장
            this.cachedData = statList;
            log.info("✅ 최종 데이터 로드 완료: {}개 구 처리됨", cachedData.size());

        } catch (Exception e) {
            log.error("범죄 데이터 로딩 중 오류 발생", e);
        }
    }

    // ================= [내부 헬퍼 메서드들] =================

    /**
     * 1단계: API 호출
     */
    private String callApi() throws Exception {
        // URL 생성 (100건 요청 - 모든 범죄 유형을 가져오기 위함)
        String requestUrl = crimeApiUrl + "?serviceKey=" + crimeApiKey + "&type=json&numOfRows=100&pageNo=1";

        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        // 응답 코드 확인
        if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
            log.error("API 호출 실패: 응답 코드 {}", conn.getResponseCode());
            return null;
        }

        // 데이터 읽기
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        return sb.toString();
    }

    /**
     * 2단계: JSON 파싱 및 집계
     */
    private Map<String, Integer> parseAndAggregate(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode items = rootNode.get("data"); // API 응답 구조: root -> data []

        Map<String, Integer> crimeMap = new HashMap<>();

        // 미리 서울 25개 구를 Map에 0으로 초기화 (데이터 누락 방지)
        for (String gu : SEOUL_COORDS.keySet()) {
            crimeMap.put(gu, 0);
        }

        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                // 항목 내부의 모든 필드를 순회 ("서울 강남구": 3, "서울 종로구": 1 ...)
                Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String key = field.getKey();
                    int count = field.getValue().asInt();

                    // "서울 "로 시작하는 키만 찾아서 처리
                    if (key.startsWith("서울 ")) {
                        String guName = key.replace("서울 ", "").trim(); // "서울 강남구" -> "강남구"

                        // 25개 구 목록에 있는 경우만 합산
                        if (crimeMap.containsKey(guName)) {
                            crimeMap.put(guName, crimeMap.get(guName) + count);
                        }
                    }
                }
            }
        }
        return crimeMap;
    }

    /**
     * 3단계: Map -> List 변환 (좌표 매핑)
     */
    private List<CrimeStats> convertToList(Map<String, Integer> map) {
        List<CrimeStats> list = new ArrayList<>();

        for (String gu : map.keySet()) {
            CrimeStats stat = new CrimeStats();
            stat.setRegion(gu);
            stat.setCrimeCount(map.get(gu));
            stat.setLat(SEOUL_COORDS.get(gu)[0]);
            stat.setLng(SEOUL_COORDS.get(gu)[1]);
            list.add(stat);
        }
        return list;
    }

    /**
     * 5단계: 등수별 등급 부여 (요청하신 로직)
     * - 리스트는 이미 범죄 건수가 적은 순(안전한 순)으로 정렬되어 있음
     */
    private void assignGradeByRank(List<CrimeStats> list) {
        // 리스트 크기 (보통 25개)
        int total = list.size();

        for (int i = 0; i < total; i++) {
            CrimeStats stat = list.get(i);
            int rank = i + 1; // 등수 (0번 인덱스 = 1등)

            if (rank <= 5) {
                // 1등 ~ 5등 (상위 5개): 안전
                stat.setGrade("SAFE");
            } else if (rank <= 15) {
                // 6등 ~ 15등 (중간 10개): 보통
                stat.setGrade("NORMAL");
            } else {
                // 16등 ~ 25등 (하위 10개): 위험
                stat.setGrade("DANGER");
            }

            // 로그 확인용
             log.info("{}등: {} ({}건) -> {}", rank, stat.getRegion(), stat.getCrimeCount(), stat.getGrade());
        }
    }
    //    ================= [리뷰 주소지의 범죄 현황] =================
    public String extractDistrict(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) {
            return "";
        }

        // 공백 기준 주소 자르기
        String[] addressParts = fullAddress.split(" ");
        // 보통 두 번째 단어인 '구' 추출
        if (addressParts.length >= 2) {
            return addressParts[1];
        }
        return "";
    }

    public static class MyCrimeInfo {
        private String crimeName; // 범죄이름
        private int count; // 건수
        private boolean isNo1; // 서울 내 1위 여부

        public MyCrimeInfo(String crimeName, int count, boolean isNo1) {
            this.crimeName = crimeName;
            this.count = count;
            this.isNo1 = isNo1;
        }

        public String getCrimeName() { return crimeName; }
        public int getCount() { return count; }
        public boolean isNo1() { return isNo1; }
    }

    // ================= [리뷰 상세페이지용 범죄 분석 로직] =================

    /**
     * 리뷰 상세페이지에 보여줄 범죄 데이터를 분석하여 DTO에 담아주는 메서드
     */
    public void analyzeCrimeForReview(ReviewDTO reviewDto) {
        try {
            // 주소에서 '구' 추출
            String district = extractDistrict(reviewDto.getAddress());
            if (district.isEmpty()) return;

            String targetKey = "서울 " + district; // JSON에서 찾을 이름

            // API 호출
            String jsonString = callApi();
            if (jsonString == null) return;

            // JSON (파싱)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode items = rootNode.get("data");

            // 모든 범죄 종목을 임시로 담을 리스트
            List<Map<String, Object>> allCrimes = new ArrayList<>();

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String crimeName = item.get("범죄중분류").asText();
                    int myCount = item.get(targetKey) != null ? item.get(targetKey).asInt() : 0;

                    // 서울 1위 찾기 로직
                    boolean isNo1 = checkIsSeoulNo1(item, targetKey, myCount);

                    // 한 줄의 정보를 바구니에 담기
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", crimeName);
                    info.put("count", myCount);
                    info.put("isNo1", isNo1);
                    allCrimes.add(info);
                }
            }

            // 건수 많은 순서로 정렬 (내림차순)
            allCrimes.sort((a, b) -> (int)b.get("count") - (int)a.get("count"));

            List<String> top3Names = new ArrayList<>();
            List<Integer> top3Counts = new ArrayList<>();
            String warning = "";

            // 상위 3개 데이터 추출 및 1위 확인
            int limit = Math.min(3, allCrimes.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> crime = allCrimes.get(i);
                String name = (String) crime.get("name");
                int count = (int) crime.get("count");
                boolean isNo1 = (boolean) crime.get("isNo1");

                top3Names.add(name);
                top3Counts.add(count);

                // 만약 이 종목이 서울 전체 1위라면 경고 문구 생성
                if (isNo1) {
                    warning = "이 지역은 서울 내 [" + name + "] 발생 건수가 가장 높은 구입니다.";
                }
            }

            // ReviewDTO의 칸(Field)에 배달 완료
            reviewDto.setTopCrimes(top3Names);
            reviewDto.setCrimeCounts(top3Counts);
            reviewDto.setSafetyWarning(warning);

        } catch (Exception e) {
            log.error("범죄 데이터 분석 중 에러 발생", e);
        }
    }

    /**
     * 해당 범죄 종목에서 우리 구가 서울 전체 1위인지 확인하는 헬퍼 메서드
     */
    private boolean checkIsSeoulNo1(JsonNode item, String targetKey, int myCount) {
        if (myCount == 0) return false;

        Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            // "서울 "로 시작하는 다른 구들의 점수와 해당 지역구 점수를 비교
            if (field.getKey().startsWith("서울 ") && !field.getKey().equals(targetKey)) {
                if (field.getValue().asInt() > myCount) {
                    return false;
                }
            }
        }
        return true;
    }



}