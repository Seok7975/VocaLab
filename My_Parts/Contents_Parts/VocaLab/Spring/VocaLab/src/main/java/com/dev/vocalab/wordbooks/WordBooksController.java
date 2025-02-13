package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesService;
import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.users.details.CustomUsersDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/WordBook")
@Controller
@RequiredArgsConstructor
public class WordBooksController {
    private final WordBooksService wordBooksService;
    private final FilesService filesService;
    private final UsersRepository usersRepository;
    private final WordBooksRepository wordBooksRepository;

    // 단어장 목록 조회 - JSP에서 직접 렌더링
    @GetMapping("/List")
    public String wordbookList(Model model) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        // 사용자 정보와 단어장 목록 조회
        UsersEntity user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        List<WordBooksEntity> wordbooks = wordBooksRepository.findByUserId(userId);
        model.addAttribute("wordbooks", wordbooks);
        model.addAttribute("userNickname", user.getUserNickname());

        return "wordbooks/wordbooksList";
    }
    // 단어장 내부 진입
    @GetMapping("/Word")
    public String wordListPage(
            @RequestParam(value = "wordBookId") Integer wordBookId,
            Model model) {
        try {
            // 인증 확인
            if (!AuthenticationUtil.isAuthenticated()) {
                return "redirect:/login";
            }
            String currentUserId = AuthenticationUtil.getCurrentUserId();
            if (currentUserId == null) {
                return "redirect:/login";
            }

            // 단어장 정보 조회 및 소유자 확인
            Optional<WordBooksEntity> wordbook = wordBooksRepository.findById(wordBookId);
            if (wordbook.isEmpty() || !wordbook.get().getUserId().equals(currentUserId)) {
                return "redirect:/WordBook/List";
            }

            // 단어 목록 조회
            List<String[]> wordList = wordBooksService.readWordBook(wordBookId);
            model.addAttribute("wordList", wordList);
            wordbook.ifPresent(wb -> model.addAttribute("wordbook", wb));

        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid WordBook ID: " + wordBookId);
            return "wordbooks/wordBooksList";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load the word book: " + e.getMessage());
        }
        return "wordbooks/wordList";
    }
    // 단어장 내부에서 데이터 불러오기 API
    @GetMapping("/WordData")
    @ResponseBody
    public List<String[]> getWordData(@RequestParam("wordBookId") Integer wordBookId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        try {
            return wordBooksService.readWordBook(wordBookId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word book data.", e);
        }
    }

    @GetMapping("/WordBookData")
    @ResponseBody
    public ResponseEntity<WordBooksDTO> getWordBookData(@RequestParam("wordBookId") Integer wordBookId) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 단어장 정보 조회
            WordBooksEntity wordBook = wordBooksService.loadWordBookStatus(wordBookId);

            // 조회된 데이터가 없는 경우
            if (wordBook == null) {
                return ResponseEntity.notFound().build();
            }

            // Entity를 DTO로 변환
            WordBooksDTO dto = WordBooksDTO.builder()
                    .wordBookId(wordBook.getWordBookId())
                    .wordBookTitle(wordBook.getWordBookTitle())
                    .bookmark(wordBook.getBookmark())
                    .userId(wordBook.getUserId())
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 단어장 내부 단어 삭제 API
    @PostMapping("/WordData/remove")
    @ResponseBody
    public void deleteWords( @RequestBody DeleteWordsRequestDTO request) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        try {
            wordBooksService.deleteWord(request.getWordBookId(), request.getWords());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete words from word book.", e);
        }
    }
//    북마크 상태 변경 1→0 0→1
    @PostMapping("/bookmark")
    @ResponseBody  // AJAX 응답을 위해 추가
    public Map<String, Object> changeBookmark(@RequestParam("wordBookId") Integer wordBookId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        wordBooksService.changeStatusOfBookmark(wordBookId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "즐겨찾기 상태가 변경되었습니다.");
        return response;
    }
    @GetMapping("/getWords")
    @ResponseBody
    public ResponseEntity<List<String>> getWordsByWordBookId(@RequestParam("wordBookId") Integer wordBookId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<String[]> wordList = wordBooksService.readWordBook(wordBookId);
            // 첫 번째 열만 추출
            List<String> firstColumnWords = wordList.stream()
                    .map(row -> row[0])  // 각 행의 첫 번째 열 선택
                    .collect(Collectors.toList());

            return ResponseEntity.ok(firstColumnWords);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/updateTitle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWordBookTitle(
            @RequestParam("wordBookId") Integer wordBookId,
            @RequestParam("newTitle") String newTitle) {

        Map<String, Object> response = new HashMap<>();

        // 1. 입력값 검증
        if (!isValidTitle(newTitle)) {
            response.put("success", false);
            response.put("message", "유효하지 않은 제목 형식입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String currentUserId = AuthenticationUtil.getCurrentUserId();

            // 3. 단어장 존재 및 소유자 확인
            WordBooksEntity wordbook = wordBooksRepository.findById(wordBookId)
                    .orElseThrow(() -> new IllegalArgumentException("단어장을 찾을 수 없습니다."));

            if (!wordbook.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 4. XSS 방지를 위한 이스케이프 처리
            String sanitizedTitle = HtmlUtils.htmlEscape(newTitle.trim());

            // 5. 제목 업데이트
            wordBooksService.updateWordBookTitle(wordBookId, sanitizedTitle);

            response.put("success", true);
            response.put("message", "단어장 제목이 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "제목 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 제목 유효성 검증 메소드
    private boolean isValidTitle(String title) {
        // null 체크
        if (title == null) {
            return false;
        }

        String trimmedTitle = title.trim();

        // 길이 체크
        if (trimmedTitle.length() < 1 || trimmedTitle.length() > 50) {
            return false;
        }

        // HTML 태그 포함 여부 체크
        if (trimmedTitle.matches(".*<[^>]*>.*")) {
            return false;
        }

        // 허용된 문자만 포함되어 있는지 체크
        return trimmedTitle.matches("^[가-힣a-zA-Z0-9\\s,.()-_]+$");
    }

}
