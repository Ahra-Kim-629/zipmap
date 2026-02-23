package com.daedong.zipmap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {


    // 1. properties에 숨겨둔 비밀키와 주소를 가져옵니다. (@Value 어노테이션 사용)
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 생성자 주입 방식 (Clean Code 권장 방식: 객체를 안전하게 초기화합니다)
    public GeminiService() {

        // 💡 5초 이상 걸리면 무조건 통신을 끊어버리는 타이머 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 서버 연결 대기 5초
        factory.setReadTimeout(5000);    //
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 특정 지역의 리뷰들을 모아서 AI에게 요약을 요청하는 메서드
     * @param regionName 지역명 (예: 관악구)
     * @param reviews 해당 지역의 리뷰 내용 리스트
     * @return AI가 작성한 요약본 텍스트
     */
    @Cacheable(value = "aiSummaryCache", key = "#regionName")
    public String summarizeReviews(String regionName, List<String> reviews) {

        // 💡 1. AI에게 내릴 명령(프롬프트) 고도화 (StringBuilder 활용)
        StringBuilder promptBuilder = new StringBuilder();

        // 페르소나(역할) 부여: AI가 어떤 말투와 관점을 가질지 정해줍니다.
        promptBuilder.append("너는 서울의 동네 분위기와 실거주 후기를 솔직하게 전달해주는 '동네 찐주민'이자 '리뷰 안내원'이야.\n\n");

        // 상황 설명 및 데이터 주입
        promptBuilder.append("다음은 '").append(regionName).append("' 지역의 실제 거주자들이 남긴 집 리뷰 데이터야. ");
        promptBuilder.append("데이터는 [장점], [단점], [리뷰 내용]으로 구성되어 있어.\n");
        promptBuilder.append("이 데이터를 종합적으로 분석해서, 방을 구하는 사람들에게 찐주민의 입장에서 아래 [출력 양식]에 맞춰 텍스트로만 깔끔하게 요약해 줘. 마크다운(*) 같은 특수기호는 쓰지 말고 이모지만 적절히 섞어줘.\n\n");

        // ★ 핵심: 출력 양식 강제하기 (AI가 딴소리를 못하게 틀을 잡아줌)
        promptBuilder.append("[출력 양식]\n");
        promptBuilder.append("🌟 주요 장점 : (가장 많이 언급된 장점 카테고리와 그 이유를 1~2줄로 설명)\n");
        promptBuilder.append("🚨 주의할 점 : (단점 카테고리와 실제 불만 사항을 1~2줄로 설명)\n");
        promptBuilder.append("💡 찐주민의 총평 : (실제 거주자들의 의견을 종합한 1줄 요약)\n\n");

        // 실제 데이터 이어 붙이기
        promptBuilder.append("[실제 리뷰 데이터]\n");
        for (int i = 0; i < reviews.size(); i++) {
            promptBuilder.append(i + 1).append(". ").append(reviews.get(i)).append("\n");
        }

        String prompt = promptBuilder.toString();
        // 2. 구글 Gemini가 요구하는 JSON 규격으로 박스 포장하기 (Map 사용)
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> partsMap = new HashMap<>();
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> textMap = new HashMap<>();

        textMap.put("text", prompt);
        partsList.add(textMap);
        partsMap.put("parts", partsList);
        contents.add(partsMap);
        requestBody.put("contents", contents);

        // 3. HTTP 헤더 세팅 (송장 작성: JSON 형식이고, API 키는 이거다!)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 구글 Gemini는 URL 끝에 ?key=발급받은키 형태로 요청을 보냅니다.
        String requestUrl = apiUrl + "?key=" + apiKey;

        // 4. 헤더와 바디를 하나로 합쳐서 우편물 완성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. 우체부(RestTemplate) 출발! POST 방식으로 보내고 결과를 String으로 받음
            String response = restTemplate.postForObject(requestUrl, requestEntity, String.class);

            // 6. 돌아온 JSON 결과 박스 뜯기 (ObjectMapper 사용)
            // AI가 준 복잡한 JSON 데이터에서 우리가 원하는 '텍스트' 알맹이만 쏙 빼냅니다.
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            return textNode.asText();

        } catch (Exception e) {
            System.out.println("AI 통신 에러 발생: " + e.getMessage());
            return "AI 요약을 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

}