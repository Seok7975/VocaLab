package com.dev.vocalab.contents.novel;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contents/novel")
public class NovelMakingController {

    private final RestTemplate restTemplate;
    private final String FLASK_API_URL = "http://localhost:5001/Python/generate-novel"; // FLASK 호출명과 같게하면됨

    public NovelMakingController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 소설 생성 페이지 보여주기
    @GetMapping("/novelMaking")
    public String novelMakingPage() {
        return "contents/novel/novelMaking";
    }

    @PostMapping("/generate-novel")  // AJAX 호출명을 같게 하면됨 // Flask 엔드포인트와 동일한 경로로 맞춤
    @ResponseBody
    public ResponseEntity<Map> generateNovel(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            System.out.println("Received Request: " + request);

            // 선택된 단어 목록 가져오기
            List<Map<String, Object>> words = (List<Map<String, Object>>) request.get("words");

            // 입력값 검증
            if (words == null || words.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No words provided"));
            }

            // Flask API로 전송할 데이터 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // words 데이터 구조 단순화
            List<Map<String, String>> formattedWords = new ArrayList<>();
            for (Map<String, Object> word : words) {
                Map<String, String> wordMap = new HashMap<>();
                wordMap.put("word", String.valueOf(word.get("word")));
                wordMap.put("meaning", String.valueOf(word.get("meaning")));
                formattedWords.add(wordMap);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("words", formattedWords);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 디버깅용 로그
            System.out.println("Sending to Flask: " + requestBody);

            // Flask API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    entity,
                    Map.class
            );

            // 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of("error", "Failed to generate novel"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in generateNovel: " + e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error generating novel: " + e.getMessage()));
        }
    }
}
