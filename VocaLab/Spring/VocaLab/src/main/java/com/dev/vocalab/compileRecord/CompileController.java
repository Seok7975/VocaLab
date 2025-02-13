package com.dev.vocalab.compileRecord;

import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.wordbooks.WordBooksEntity;
import com.dev.vocalab.wordbooks.WordBooksRepository;
import com.dev.vocalab.wordbooks.WordBooksService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping("/compile")
@Controller
@RequiredArgsConstructor
public class CompileController {

    private final CompileService compileService;
    private final WordBooksRepository wordBooksRepository;
    private final WordBooksService wordBooksService;
    private final FilesRepository filesRepository;


    // [ 단어 선택 페이지 이동 메서드 - WordBookService ] -미완:compileRecord 중 csv파일 불러와서 단어와 뜻 데이터 뿌리기
    @RequestMapping("/result")
    public String resultForm(HttpSession session, Map<String,Object> map, CompileRecordEntity com, FilesEntity files,
                             HttpServletRequest request, @RequestParam("files")List<MultipartFile> multipartFiles, Model model) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        // 사용자 세션 정보를 모델에 추가
        AuthenticationUtil.addUserSessionToModel(model);
        System.out.println("resultForm");
        // < compilePro - insert compileRecord, insert originalFile, insert resultFile  >
        com.setUserId(userId);
        List<Map<String, Object>> response = compilePro(com, files, multipartFiles);
        System.out.println("result - response : " + response.toString());

        // < 단어장 목록 불러오기 >
        List<WordBooksEntity> wordBookList = wordBooksService.getWordBooks(userId);
        System.out.println("wordBookList" + wordBookList.toString());
        map.put("books", wordBookList);
        map.put("response", response);
        // < 단어 csv파일 불러오기 >
        return "compile/compile_result";
    } //Result

    @Transactional
    public List<Map<String, Object>> compilePro(CompileRecordEntity com, FilesEntity files, List<MultipartFile> multipartFiles) {
        System.out.println("compilePro");
        System.out.println("urlTextData : " + com.getSource());
        String userId = AuthenticationUtil.getCurrentUserId();
        List<Map<String, Object>> wordList = new ArrayList<>();

        try {
            // 2개 이상의 파일이므로 getParts() 메서드를 통해 폼값을 받음
            // < 1. 단어 추출 기록, 원본파일, 단어장, 단어  insert >
            wordList = compileService.compileProService(com, files, multipartFiles, userId);
            System.out.println("compilePro - compileRecord, Files insert 성공");

        } catch( Exception e ) {
            System.out.println("파일 업로드 실패");
        }

        return wordList;
    }// compilePro 메서드

    // [ 단어장 생성하기 - wordBook ] -완성
    @RequestMapping("/addWordbook")
    @ResponseBody
    public WordBooksEntity addWordbookAjax(HttpSession session, @RequestParam("wordBookTitle") String wordBookTitle) {
        System.out.println("addWordbookAjax");
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        WordBooksEntity wordBook = new WordBooksEntity();
        wordBook.setUserId(userId);
        wordBook.setWordBookTitle(wordBookTitle);

        // [ WordBook INSERT ]
        wordBook = wordBooksRepository.save(wordBook);

        return wordBook;

    } //addWordbookAjax

    // [ 사용자 단어장 삭제 - wordBookService ] -완성
    @RequestMapping("/removeWordbook")
    @ResponseBody
    public String removeWordbookAjax(HttpSession session, @RequestBody Map<String, List<String>> map) {
        System.out.println("removeWordbookAjax");
        System.out.println("map" + map.toString());
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        String result = "";

        List<String> wordBookIds = map.get("ids");
        System.out.println("wordBookIds" + wordBookIds);
        for(String wordBookIdStr : wordBookIds) {
            int wordBookId = Integer.parseInt(wordBookIdStr);
            result = wordBooksService.deleteWordbooks(wordBookId, userId);
        }
        return result;
    }// removeWordbook

    //  [ 단어 추가 버튼 클릭시 - CompileService ] -미완:일단 /Result에서 선택된 단어를 csv파일로 저장하기
    @PostMapping("/addWordBookData")
    public String addWordBookData(HttpSession session,
                               @RequestParam("wordBookId") int wordBookId,
                               @RequestParam("word") List<String> word,
                               @RequestParam("meaning") List<String> meaning) {
        System.out.println("addWordBookData");
        System.out.println("wordBookId" + wordBookId);
        System.out.println("word" + word);
        System.out.println("meaning" + meaning);
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
    //  < 저장할 데이터 csv로 저장 >
        wordBooksService.addWords(userId, wordBookId, word, meaning);

        return "redirect:/";
    } // addWordBookData 메서드


} // class
