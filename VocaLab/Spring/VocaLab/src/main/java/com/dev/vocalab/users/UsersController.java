package com.dev.vocalab.users;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UsersController {

    private final UsersService usersService;
    private final UsersRepository usersRepository;

    // GET 방식으로 로그인 페이지 접근
    @GetMapping("/login")
    public String loginPage() {
        List<String> result = usersRepository.findAllUserId();
        return "users/login";
    }

    // 로그인 기능 => 스프링 시큐리티에서 처리되므로 필요없음
//    @PostMapping("/login")
//    String loginID(@RequestParam String userId, @RequestParam String userPassword, Model model) {
//        usersService.loginUser(userId, userPassword);
//        return "users/login";
//    }

    // GET 방식으로 회원가입 페이지 접근
    @GetMapping("/register")
    public String registerPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/";
        }
        return "users/registration";
    }

    // 회원가입 기능
    @PostMapping("/register")
    String registerID(String userId
            , String userName, String userPassword, String userNickname,
                      String userEmail, String year, String month, String day, Integer gender,
                      Model model) {

        usersService.registerUser(userId, userName, userPassword, userNickname, userEmail, year, month, day, gender);

        return "redirect:/login";
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        UsersDTO usersDTO = usersService.getUserInfo();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (usersDTO != null) {
            model.addAttribute("loginType", usersDTO.getLoginType());
            model.addAttribute("userId", usersDTO.getUserId());
            model.addAttribute("userName", usersDTO.getUserName());
            model.addAttribute("userNickname", usersDTO.getUserNickname());
            model.addAttribute("users", usersRepository.findAll());

            model.addAttribute("principal", principal);

            return "redirect:/";
        }

        return "redirect:/";
    }


    // 로그아웃 회원탈퇴 테스트용
    @GetMapping("/test")
    public String testPage(Authentication authentication, Model model) {

        UsersDTO usersDTO = usersService.getUserInfo();
        Object principal = authentication.getPrincipal();

        model.addAttribute("loginType", usersDTO.getLoginType());
        model.addAttribute("principal", principal);

        return "users/test";
    }

    // 회원 탈퇴 기능
    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteUser(Authentication authentication, HttpServletRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            //UsersDTO usersDTO = usersService.getUserInfo();

            // OAuth 회원이든 일반 회원이든 동일한 방식으로 삭제 가능
            // userSocial 필드는 Users 테이블 내에 있으므로 따로 처리할 필요 없음

            // 디버깅 로그
            String userId = authentication.getName();
            System.out.println("Attempting to delete user with ID: " + userId);
            System.out.println("Authentication type: " + authentication.getClass().getName());
            System.out.println("Principal type: " + authentication.getPrincipal().getClass().getName());

            usersService.deleteUser(authentication, request);

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    // 아이디 찾기 페이지 이동
    @GetMapping("/findId")
    public String findIdPage() {
        return "users/findId";
    }

    // 아이디 찾기 - 인증번호 발송
    @PostMapping("/api/findId/send-verification")
    @ResponseBody
    public ResponseEntity<?> sendVerificationForFindId(@RequestBody Map<String, String> request) {
        String userEmail = request.get("email");

        try {
            return usersService.sendVerificationForFindId(userEmail);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", e.getMessage()));
        }
    }

    // 아이디 찾기 - 인증번호 확인
    @PostMapping("/api/findId/verify")
    @ResponseBody
    public ResponseEntity<?> verifyCodeForFindId(@RequestBody Map<String, String> request) {
        String verificationId = request.get("verificationId");
        String code = request.get("code");
        String email = request.get("email");

        try {
            return usersService.verifyCodeForFindId(verificationId, code, email);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", e.getMessage()));
        }
    }

    // 비밀번호 찾기 페이지 이동
    @GetMapping("/findPassword")
    public String findPasswordPage() {
        return "users/findPassword";
    }

    @PostMapping("/api/findPassword/send-verification")
    public ResponseEntity<?> sendVerificationForFindPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String email = request.get("email");
        return usersService.sendVerificationForFindPassword(userId, email);
    }

    @PostMapping("/api/findPassword/verify")
    public ResponseEntity<?> verifyCodeForFindPassword(@RequestBody Map<String, String> request) {
        String verificationId = request.get("verificationId");
        String code = request.get("code");
        String userId = request.get("userId");
        String email = request.get("email");
        return usersService.verifyCodeForFindId(verificationId, code, email);
    }

    @PostMapping("/api/findPassword/change")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String newPassword = request.get("newPassword");
        return usersService.changePassword(userId, newPassword);
    }

}




/*
컨트롤러에서 비즈니스 로직 처리
    @GetMapping("/main")
    public String mainPage(Model model) {
//        // 로그인 상태 확인
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                model.addAttribute("loginType", "normal");
                model.addAttribute("userId", userDetails.getUserId());
                model.addAttribute("userName", userDetails.getUserName());
                model.addAttribute("nickname", userDetails.getUserNickname());
            } else if (principal instanceof CustomOAuth2Users) {
                CustomOAuth2Users oauth2User = (CustomOAuth2Users) principal;
                model.addAttribute("loginType", "oauth2");
                model.addAttribute("nickname", oauth2User.getNickname());
            } else if (principal instanceof CustomOIDCUsers) {
                CustomOIDCUsers oidcUser = (CustomOIDCUsers) principal;
                model.addAttribute("loginType", "oidc");
                model.addAttribute("nickname", oidcUser.getNickname());
            }

            model.addAttribute("principal", principal);
            model.addAttribute("users", usersRepository.findAll());
            return "board/csmain";
        }
        return "board/csmain";
    }
*/

