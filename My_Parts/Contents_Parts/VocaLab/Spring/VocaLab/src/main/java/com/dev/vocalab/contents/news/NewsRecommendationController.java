package com.dev.vocalab.contents.news;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
@RequestMapping("/contents/news")
public class NewsRecommendationController {

    @Value("${python.server.url}")
    private String pythonServerUrl;

    private final RestTemplate restTemplate;
    private final String FLASK_API_URL = "http://localhost:5001/Python/generate-news"; // FLASK 호출명과 같게하면됨


    public NewsRecommendationController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 뉴스 추천 페이지 보여주기
    @GetMapping("/newsRecommendation") // /contents/news/newsRecommendation로 매핑
    public String newsRecommendationPage(Model model) {
        return "contents/news/newsRecommendation";  // JSP 파일 경로
    }

    // 뉴스 추천 API 호출
    @PostMapping("/generate-news") // AJAX 호출명을 같게 하면됨 // 공부용으로 일부러 플라스크 엔드포인트명과 다르게 함
    @ResponseBody
    public ResponseEntity<Map> generateTest(@RequestBody Map<String, String> request, HttpSession session) {
        try {

            System.out.println("Received Request: " + request);

            // Python 서버로 보낼 요청 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 데이터 구성
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("word", request.get("word"));

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Python 서버 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "뉴스 추천 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    // 서버 상태 확인
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    pythonServerUrl + "/health",
                    Map.class
            );
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
