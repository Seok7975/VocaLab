package com.dev.vocalab.myPage;

import com.dev.vocalab.compileRecord.CompileDTO;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.wordbooks.WordBooksEntity;
import com.dev.vocalab.wordbooks.WordBooksService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/myPage")
@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final UsersRepository usersRepository;
    private final WordBooksService wordBookService;
    private final MyPageService myPageService;
    private final BCryptPasswordEncoder passwordEncoder;


    // [ 단어기록 ]
    @RequestMapping("/compileHistory")
    public String myPageRecord(Model model) {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);
        String userId = AuthenticationUtil.getCurrentUserId();

        List<CompileDTO> recordList = myPageService.getCompileRecordList(userId);
        System.out.println("recordList: " + recordList);

        model.addAttribute("recordList", recordList);

        return "mypage/myPageHistory";
    }

    // [ 기록중 다시 단어추가 기능 ]
    @RequestMapping("/recordAddWords")
    public String recordAddWords(Map<String,Object> map, @RequestParam("compileId")int compileId, Model model) {
        System.out.println("recordAddWords");
        System.out.println("compileId: " + compileId);

        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);
        String userId = AuthenticationUtil.getCurrentUserId();

        // < 단어장 목록 불러오기 >
        List<WordBooksEntity> wordBookList = wordBookService.getWordBooks(userId);
        System.out.println("wordBookList" + wordBookList.toString());
        map.put("books", wordBookList);

        return "compile/compile_result";
    }

    // [ 회원정보 ]
    @GetMapping("/userInformation")
    public String myPageUserInfo(Model model) {

        System.out.println("myPageUserInfo");
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);
        String userId = AuthenticationUtil.getCurrentUserId();

        System.out.println("userId: " + userId);
        Optional<UsersEntity> users = usersRepository.findByUserId(userId);
        System.out.println("users: " + users.toString());
        String userBirthday = users.get().getBirthDate();
        System.out.println("userBirthday: " + userBirthday);

        String year = userBirthday.substring(0, 4);
        String month = userBirthday.substring(4, 6);
        String day = userBirthday.substring(6, 8);

        String birth = year + "-" + month + "-" + day;

        System.out.println("birth: " + birth);

        model.addAttribute("user", users.get());
        model.addAttribute("birth", birth);

        return "mypage/myPageUserInfo";
    }

//    @PostMapping("/checkPassword")
//    public String checkPassword(@RequestParam("password") String password, Model model) {
//        if (!AuthenticationUtil.isAuthenticated()) {
//            return "redirect:/login";
//        }
//        String userId = AuthenticationUtil.getCurrentUserId();
//        System.out.println("userId: " + userId);
//        Optional<UsersEntity> users = usersRepository.findByUserId(userId);
//
////        if (users.get().getUserPassword().equals(password)) {}
//        return "redirect:userModify";
//
//    }

    // [ 회원정보수정 뷰 ]
    @GetMapping("/userModify")
    public String myPageUserModify(Model model) {
        System.out.println("myPageUserModify");
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);
        String userId = AuthenticationUtil.getCurrentUserId();

        System.out.println("userId: " + userId);
        Optional<UsersEntity> users = usersRepository.findByUserId(userId);

        String userBirthday = users.get().getBirthDate();
        String year = userBirthday.substring(0, 4);
        String month = userBirthday.substring(4, 6);
        String day = userBirthday.substring(6, 8);

        String birth = year + "-" + month + "-" + day;

        model.addAttribute("user", users.get());
        model.addAttribute("birth", birth);

        return "mypage/myPageUserModify";
    }

    // [ 회원정보 수정 버튼 ] - test 해봐야함
    @PostMapping("/userModifyPro")
    public String myPageUserModifyPro(Model model,
                                      @RequestParam("userNickname") String nickName) {
        System.out.println("myPageUserModifyPro");
        System.out.println("nickName: " + nickName);
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();

        myPageService.modifyNickName(userId,nickName);

        return "redirect:userInformation";
    }

    // [ 비밀번호 입력 후 확인 ]
    @PostMapping("/checkPassword")
    @ResponseBody
    public String checkPassword(@RequestParam("userPassword") String password) {
        System.out.println("checkPassword");
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();

        System.out.println("password : " + password);

        UsersEntity user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            return "fail"; // 비밀번호 불일치 시 "fail" 반환
        }

        return "success"; // 비밀번호 일치 시 "success" 반환
    }

} // class
