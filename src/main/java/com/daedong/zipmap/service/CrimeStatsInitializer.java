package com.daedong.zipmap.service;

import com.daedong.zipmap.domain.CrimeStat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 앱 실행 시 서울 구별 범죄 데이터를 공공 API에서 가져와 DB에 저장하는 초기화 컴포넌트
 */
@Component
@RequiredArgsConstructor
public class CrimeStatsInitializer implements ApplicationRunner {

    private final CrimeStatsService crimeStatsService; // DB 저장/조회 서비스

    @Value("${public.data.crime.url:}")
    private String crimeApiUrl;

    @Value("${public.data.crime.key:}")
    private String crimeApiKey;

    @Override
    public void run(ApplicationArguments args) {
        if (crimeApiUrl.isEmpty() || crimeApiKey.isEmpty()) {
            System.out.println("⚠️ CrimeStatsInitializer: API URL 또는 KEY가 설정되지 않아 초기화 스킵");
            return;
        }

        try {
            String requestUrl = crimeApiUrl + "?serviceKey=" + crimeApiKey + "&type=json";
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) throw new RuntimeException("API 호출 실패: " + responseCode);

            Scanner scanner = new Scanner(url.openStream(), "UTF-8");
            StringBuilder jsonResponse = new StringBuilder();
            while (scanner.hasNext()) jsonResponse.append(scanner.nextLine());
            scanner.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse.toString());
            JsonNode dataArray = rootNode.get("data");

            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode objNode : dataArray) {
                    objNode.fields().forEachRemaining(entry -> {
                        String regionName = entry.getKey();
                        int crimeCount = entry.getValue().asInt();

                        if (regionName.startsWith("서울특별시")) {
                            double lat = 0.0; // 실제 좌표 필요 시 Geocoding
                            double lng = 0.0;

                            // DB 저장
                            crimeStatsService.saveCrimeStats(regionName, crimeCount, lat, lng);
                        }
                    });
                }
            }
            System.out.println("✅ 서울 구별 범죄 데이터 초기화 완료!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
