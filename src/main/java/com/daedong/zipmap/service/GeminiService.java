package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.PostDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    @Value("${gemini.api.key.post}")
    private String postapiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.url.post}")
    private String postgetapiUrl;

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

    /**
     * 현재 보고 있는 카테고리의 게시글 리스트를 한꺼번에 분석하여 요약합니다.
     */
    @Cacheable(value = "aiSummaryCache", key = "#category + '-' + #keyword + '-' + #location")
    public String summarizeCategory(String category, List<PostDTO> posts) {
        if (posts == null || posts.isEmpty()) {
            return "현재 분석할 게시글이 없습니다. 게시글을 먼저 등록해 주세요! 😊";
        }

        StringBuilder promptBuilder = new StringBuilder();

        // 1. 페르소나 부여
        promptBuilder.append("너는 자취생들의 커뮤니티 게시판을 분석해주는 '친절한 커뮤니티 매니저'야.\n\n");

        // 2. 상황 설명
        String categoryName = switch (category != null ? category : "ALL") {
            case "GROUP" -> "공동구매";
            case "SHARE" -> "나눔";
            case "FREE" -> "자유";
            default -> "전체";
        };
        promptBuilder.append("다음은 '").append(categoryName).append("' 게시판에 올라온 최신 게시글 목록이야. ");
        promptBuilder.append("이 글들을 분석해서 현재 게시판의 분위기와 주요 이슈를 파악해줘.\n\n");

        // 3. 출력 양식 강제
        promptBuilder.append("[출력 양식]\n");
        if ("GROUP".equals(category)) {
            promptBuilder.append("🛒 핫딜 트렌드 : (현재 인기 있는 공구 품목과 가격대 요약)\n");
            promptBuilder.append("💡 참여 팁 : (공구 참여 시 주의할 점이나 팁)\n");
        } else if ("SHARE".equals(category)) {
            promptBuilder.append("🎁 나눔 현황 : (주로 어떤 물품들이 나눔되고 있는지 요약)\n");
            promptBuilder.append("🏃 경쟁률 : (나눔이 얼마나 빠르게 마감되는지 분위기 파악)\n");
        } else {
            promptBuilder.append("🔥 핫 이슈 : (가장 많이 언급되는 주제나 고민 요약)\n");
            promptBuilder.append("🗣️ 분위기 : (게시판의 전반적인 분위기 - 활기참, 진지함 등)\n");
        }
        promptBuilder.append("📝 3줄 요약 : (전체적인 내용을 3줄로 깔끔하게 정리)\n\n");

        // 4. 데이터 주입
        promptBuilder.append("[게시글 데이터]\n");
        int limit = Math.min(posts.size(), 10);
        for (int i = 0; i < limit; i++) {
            PostDTO p = posts.get(i);
            String cleanContent = p.getContent().replaceAll("<[^>]*>", "");
            String summaryContent = cleanContent.substring(0, Math.min(cleanContent.length(), 100));
            promptBuilder.append(i + 1).append(". 제목: ").append(p.getTitle())
                    .append(" (내용: ").append(summaryContent).append("...)\n");
        }

        return callGemini(promptBuilder.toString());
    }

    // [추가] 선택된 게시글들만 요약하는 메서드
    public String summarizeSelectedPosts(List<PostDTO> posts) {
        if (posts == null || posts.isEmpty()) {
            return "선택된 게시글이 없습니다. 요약할 글을 선택해주세요! 😊";
        }

        StringBuilder promptBuilder = new StringBuilder();

        // 1. 페르소나 부여
        promptBuilder.append("너는 사용자가 선택한 게시글들을 꼼꼼하게 읽고 핵심만 짚어주는 '스마트 요약 비서'야.\n\n");

        // 2. 상황 설명
        promptBuilder.append("사용자가 관심 있어 하는 게시글들을 모아봤어. 이 글들의 공통된 주제나 특징이 있는지, 아니면 각각 어떤 유용한 정보를 담고 있는지 분석해줘.\n\n");

        // 3. 출력 양식 강제
        promptBuilder.append("[출력 양식]\n");
        promptBuilder.append("📌 핵심 주제 : (선택된 글들을 관통하는 키워드나 주제)\n");
        promptBuilder.append("💡 주요 내용 : (각 글의 핵심 내용을 통합하여 설명)\n");
        promptBuilder.append("✨ 비서의 한마디 : (이 글들을 읽은 사용자에게 도움이 될 만한 조언이나 코멘트)\n\n");

        // 4. 데이터 주입
        promptBuilder.append("[선택된 게시글 데이터]\n");
        for (int i = 0; i < posts.size(); i++) {
            PostDTO p = posts.get(i);
            String cleanContent = p.getContent().replaceAll("<[^>]*>", "");
            String summaryContent = cleanContent.substring(0, Math.min(cleanContent.length(), 150));
            promptBuilder.append(i + 1).append(". [").append(p.getCategory()).append("] 제목: ").append(p.getTitle())
                    .append("\n   내용: ").append(summaryContent).append("...\n");
        }

        return callGemini(promptBuilder.toString());
    }

    // 공통 API 호출 메서드 (기존 로직 재사용을 위해 분리하거나, 여기서 직접 호출)
    // 공통 API 호출 메서드
    private String callGemini(String prompt) {
        // 1. 요청 바디(JSON 구조) 만들기
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> partsMap = new HashMap<>();
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> textMap = new HashMap<>();

        textMap.put("text", prompt);
        partsList.add(textMap);
        partsMap.put("parts", partsList); // 오타 주의: partsMap.put("parts", partsList);
        contents.add(partsMap);
        requestBody.put("contents", contents);

        // 2. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. [수정 포인트] properties에서 가져온 URL과 키를 결합
        // postgetapiUrl이 "https://.../gemini-1.5-flash:generateContent" 형태라고 가정합니다.
        String urlToUse = postgetapiUrl + "?key=" + postapiKey;

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. API 호출
            String response = restTemplate.postForObject(urlToUse, requestEntity, String.class);

            // 5. JSON 파싱 (알맹이 텍스트만 추출)
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            return textNode.asText();

        } catch (HttpClientErrorException.TooManyRequests e) {
            return "AI 사용량이 많아 잠시 쉬고 있어요. 1분 뒤에 다시 시도해주세요! 😅";
        } catch (Exception e) {
            // 상세한 에러 로그 출력 (디버깅용)
            System.err.println("AI 통신 중 상세 에러: " + e.getMessage());
            return "AI 요약을 불러오는 중 오류가 발생했습니다. (관리자에게 문의하세요)";
        }
    }

}