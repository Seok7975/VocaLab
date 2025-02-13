package com.dev.vocalab.users;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.details.CustomUsersDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void loginUser(String userId, String userPassword) {
        UsersEntity users = new UsersEntity();
        users.setUserId(userId);
        users.setUserPassword(passwordEncoder.encode(userPassword));

        System.out.println("로그인 되었습니다(userId, userPassword): " + userId + ", " + userPassword);
    }

    // 회원가입 처리
    public void registerUser(String userId
            , String userName, String userPassword, String userNickname,
                                    String userEmail, String year, String month, String day, Integer gender){

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
                userNickname  + " " + userEmail + " " + birthDate + " " + gender);
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
}
