package com.dev.vocalab.users;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.details.CustomUsersDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender emailSender; // 이메일 발송을 위한 의존성 추가

    // 인증 정보 저장소
    private final Map<String, VerificationInfo> verificationStore = new ConcurrentHashMap<>();


    public void loginUser(String userId, String userPassword) {
        UsersEntity users = new UsersEntity();
        users.setUserId(userId);
        users.setUserPassword(passwordEncoder.encode(userPassword));

        System.out.println("로그인 되었습니다(userId, userPassword): " + userId + ", " + userPassword);
    }

    // 회원가입 처리
    public void registerUser(String userId
            , String userName, String userPassword, String userNickname,
                             String userEmail, String year, String month, String day, Integer gender) {

        UsersEntity users = new UsersEntity();

        users.setUserId(userId);
        users.setUserName(userName);
        users.setUserPassword(passwordEncoder.encode(userPassword));
        users.setUserNickname(userNickname);
        users.setUserEmail(userEmail);

        // 생년월일 형식 변환 (YYYY-MM-DD)
        String birthDate = String.format("%s-%s-%s",
                year,
                month.length() == 1 ? "0" + month : month,
                day.length() == 1 ? "0" + day : day
        );
        users.setBirthDate(birthDate);

        // 성별 코드 변환
        int birthYear = Integer.parseInt(year);
        int formattedGender;
        if (birthYear < 2000) {
            // 2000년 이전 출생
            formattedGender = (gender == 1) ? 1 : 2;
        } else {
            // 2000년 이후 출생
            formattedGender = (gender == 1) ? 3 : 4;
        }
        users.setGender(formattedGender);

        usersRepository.save(users);

        System.out.println
                ("회원가입 되었습니다(userId, userName, userPassword, userNickname, userEmail, birthDate, gender): "
                        + userId + " " + userName + " " + userPassword + " " +
                        userNickname + " " + userEmail + " " + birthDate + " " + gender);
    }

    //
    public UsersDTO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsersDTO usersDTO = new UsersDTO();

        System.out.println("Authentication: " + authentication);

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
//            UsersDTO usersDTO = new UsersDTO();

            System.out.println("Principal class: " + principal.getClass().getName());
            System.out.println("Principal: " + principal);

            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                usersDTO.setLoginType("normal");
                usersDTO.setUserId(userDetails.getUserId());
                usersDTO.setUserName(userDetails.getUserName());
                usersDTO.setUserNickname(userDetails.getUserNickname());
                return usersDTO;
            } else if (principal instanceof CustomOAuth2Users) {
                CustomOAuth2Users oauth2User = (CustomOAuth2Users) principal;
                usersDTO.setLoginType("oauth2");
                usersDTO.setUserId(oauth2User.getUserId());
                usersDTO.setUserName(oauth2User.getUserName());
                usersDTO.setUserNickname(oauth2User.getUserNickname());
                return usersDTO;
            } else if (principal instanceof CustomOIDCUsers) {
                CustomOIDCUsers oidcUser = (CustomOIDCUsers) principal;
                usersDTO.setLoginType("oidc");
                usersDTO.setUserId(oidcUser.getUserId());
                usersDTO.setUserName(oidcUser.getUserName());
                usersDTO.setUserNickname(oidcUser.getUserNickname());
                return usersDTO;
            }
        }

        return null;
    }

    public void deleteUser(Authentication authentication, HttpServletRequest request) {
        String userId;

        try {

            if (authentication.getPrincipal() instanceof CustomOIDCUsers) {
                CustomOIDCUsers oidcUser = (CustomOIDCUsers) authentication.getPrincipal();
                // 이메일을 사용하여 조회
                userId = oidcUser.getOAuth2Response().getUserEmail();
            } else if (authentication.getPrincipal() instanceof CustomOAuth2Users) {
                CustomOAuth2Users oauth2User = (CustomOAuth2Users) authentication.getPrincipal();
                // 이메일을 사용하여 조회
                userId = oauth2User.getOAuth2Response().getUserEmail();
            } else {
                // 일반 로그인의 경우
                userId = authentication.getName();
            }

            // 사용자 존재 여부 확인
            UsersEntity usersEntity = usersRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 사용자와 관련된 데이터 삭제
            // 예: 게시글, 댓글 등 연관된 데이터 삭제
            // itemRepository.deleteAllByUserName(username);
            // commentRepository.deleteAllByUserName(username);

            // 사용자 계정 삭제
            usersRepository.delete(usersEntity);

            // 세션 무효화
            SecurityContextHolder.getContext().setAuthentication(null);
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.", e);
        }

    }

    // 인증 타입을 구분하기 위한 enum
    public enum VerificationType {
        FIND_ID("아이디 찾기"),
        FIND_PASSWORD("비밀번호 찾기");

        private final String description;

        VerificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 아이디 찾기 - 인증번호 발송
    public ResponseEntity<?> sendVerificationForFindId(String userEmail) {
        try {
            System.out.println("Attempting to send verification email to: " + userEmail);

            // 이메일 존재 여부 확인
            if (!usersRepository.existsByUserEmail(userEmail)) {

                System.out.println("Email not found in database: " + userEmail);

                return ResponseEntity.badRequest()
                        .body(Map.of("success", false,
                                "message", "등록되지 않은 이메일입니다."));
            }

            // 인증 코드 생성 (6자리)
            String code = generateVerificationCode();
            System.out.println("Generated verification code: " + code);

            // 인증 정보 저장 (5분 유효)
            saveVerificationInfo(userEmail, code);
            System.out.println("Verification info saved");

            try {
                // 이메일 발송
                sendVerificationEmail(userEmail, code, VerificationType.FIND_ID);
                System.out.println("Verification email sent successfully");
            } catch (Exception e) {
                System.err.println("Error sending email: ");
                e.printStackTrace();
                throw e;
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증번호가 발송되었습니다.",
                    "verificationId", userEmail
            ));
        } catch (Exception e) {
            System.err.println("Error in sendVerificationForFindId: ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "인증번호 발송 중 오류가 발생했습니다."));
        }
    }

    // 이메일 발송 메서드
    private void sendVerificationEmail(String to, String code, VerificationType type) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setFrom("seok7975@naver.com"); // 반드시 application.properties의 username과 동일해야 함
        helper.setTo(to);

        // 인증 타입에 따른 제목 설정
        String subject = String.format("VocaLab %s 인증번호", type.getDescription());
        helper.setSubject(subject);

        // 인증 타입에 따른 본문 설정
        String content = String.format(
                "안녕하세요.\nVocaLab %s를 위한 인증번호입니다.\n\n인증번호: %s\n\n이 인증번호는 5분간 유효합니다.",
                type.getDescription(),
                code
        );
        helper.setText(content);

        emailSender.send(mimeMessage);
    }

    // 인증번호 생성
    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    // 인증 정보 저장
    private void saveVerificationInfo(String email, String code) {
        VerificationInfo verificationInfo = new VerificationInfo(
                email, code, LocalDateTime.now().plusMinutes(5)
        );
        verificationStore.put(email, verificationInfo);
    }

    // 아이디 찾기 - 인증번호 확인
    public ResponseEntity<?> verifyCodeForFindId(String verificationId, String code, String email) {
        VerificationInfo verificationInfo = verificationStore.get(verificationId);

        if (verificationInfo == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "유효하지 않은 인증 정보입니다."));
        }

        if (verificationInfo.isExpired()) {
            verificationStore.remove(verificationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "인증 시간이 만료되었습니다."));
        }

        if (!verificationInfo.getCode().equals(code)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "잘못된 인증번호입니다."));
        }

        // 인증 성공, 사용자 ID 조회
        UsersEntity user = usersRepository.findByUserEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false,
                            "message", "사용자 정보를 찾을 수 없습니다."));
        }

        verificationStore.remove(verificationId); // 인증 정보 삭제

        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", user.getUserId()
        ));
    }

    // 비밀번호 찾기 - 인증번호 발송
    public ResponseEntity<?> sendVerificationForFindPassword(String userId, String userEmail) {
        try {
            // 사용자 ID와 이메일이 일치하는지 확인
            UsersEntity user = usersRepository.findByUserId(userId)
                    .orElse(null);

            if (user == null || !user.getUserEmail().equals(userEmail)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false,
                                "message", "입력하신 정보와 일치하는 계정이 없습니다."));
            }

            // 인증 코드 생성 및 저장
            String code = generateVerificationCode();
            saveVerificationInfo(userEmail, code);

            // 이메일 발송
            sendVerificationEmail(userEmail, code, VerificationType.FIND_PASSWORD);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증번호가 발송되었습니다.",
                    "verificationId", userEmail
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "인증번호 발송 중 오류가 발생했습니다."));
        }
    }

    // 비밀번호 변경
    public ResponseEntity<?> changePassword(String userId, String newPassword) {
        try {
            UsersEntity user = usersRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 새 비밀번호 암호화 및 저장
            user.setUserPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 성공적으로 변경되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "비밀번호 변경 중 오류가 발생했습니다."));
        }
    }
}
