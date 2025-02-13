package com.dev.vocalab.contents.wordTest;

import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.wordbooks.WordBooksDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
@RequestMapping("/contents/wordTest")
public class WordTestController {

    private final RestTemplate restTemplate;
    private final String FLASK_API_URL = "http://im.21v.in:28116/Python/generate-test"; // FLASK 호출명과 같게하면됨

    public WordTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 테스트 페이지 보여주기
    @GetMapping("/wordTest") // /contents/wordTest/wordTest로 매핑
    public String wordTestPage(Model model) {
        // 사용자 정보를 세션에 추가
        AuthenticationUtil.addUserSessionToModel(model);
        return "contents/wordTest/wordTest";  // JSP 파일 경로
    }

    // 테스트 데이터 생성
    @PostMapping("/generate-test") // AJAX 호출명을 같게 하면됨 // Flask 엔드포인트와 동일한 경로로 맞춤
    @ResponseBody
    public ResponseEntity<Map> generateTest(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            System.out.println("Received Request: " + request);
            // type과 words 데이터 추출
            String testType = (String) request.get("test_type");
            List<Map<String, Object>> words = (List<Map<String, Object>>) request.get("words");

            // 입력값 검증
            if (testType == null || !Arrays.asList("meaning", "word").contains(testType)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid test type"));
            }

            if (words == null || words.size() < 20) {
                return ResponseEntity.badRequest().body(Map.of("error", "At least 20 words are required"));
            }

            // Flask API로 전송할 데이터 준비
            Map<String, Object> flaskRequest = new HashMap<>();
            flaskRequest.put("test_type", testType);

            // words 데이터 구조 단순화 - Flask 서버가 기대하는 형식으로 변환
            List<Map<String, String>> formattedWords = new ArrayList<>();
            for (Map<String, Object> word : words) {
                Map<String, String> wordMap = new HashMap<>();
                wordMap.put("word", String.valueOf(word.get("word")));
                wordMap.put("meaning", String.valueOf(word.get("meaning")));
                // type과 example은 제외 (Flask 서버가 사용하지 않음)
                formattedWords.add(wordMap);
            }
            flaskRequest.put("words", formattedWords);

            // 디버깅용 로그
            System.out.println("Sending to Flask: " + flaskRequest);

            // Flask API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    flaskRequest,
                    Map.class
            );

            // 응답이 성공적이고 데이터가 있는 경우
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 테스트 페이지로 리다이렉트하기 위한 URL 추가
                Map<String, Object> resultMap = new HashMap<>(response.getBody());
                resultMap.put("redirectUrl", "/contents/wordTest/wordTest");

                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of("error", "Failed to generate test"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in generateTest: " + e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error generating test: " + e.getMessage()));
        }
    }
}