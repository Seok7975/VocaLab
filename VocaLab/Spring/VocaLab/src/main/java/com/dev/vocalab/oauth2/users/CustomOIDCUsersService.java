package com.dev.vocalab.oauth2.users;

import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOIDCUsersService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UsersRepository usersRepository;

    public CustomOIDCUsersService(UsersRepository usersRepository) {
        System.out.println("CustomOIDCUsersService(Google) 빈 등록 완료");
        this.usersRepository = usersRepository;
    }

    // 구글용 gender 변환 메서드
    private int convertGender(String provider, String userGender, String birthYear) {
        try {
            // birthYear가 0000이거나 unknown인 경우 기본값 처리
            if ("0000".equals(birthYear) || birthYear == null) {
                // 출생연도를 알 수 없는 경우 1900년대생으로 가정
                switch (userGender.toLowerCase()) {
                    case "male":
                        return 1;   // 1900년대 남성
                    case "female":
                        return 2;   // 1900년대 여성
                    default:
                        return 0;   // 성별 미상
                }
            }

            int year = Integer.parseInt(birthYear);
            boolean isBornAfter2000 = year >= 2000;

            if ("google".equals(provider)) {
                switch (userGender.toLowerCase()) {
                    case "male":
                        return isBornAfter2000 ? 3 : 1;   // 2000년대 남성 : 1900년대 남성
                    case "female":
                        return isBornAfter2000 ? 4 : 2;   // 2000년대 여성 : 1900년대 여성
                    default:
                        return 0;   // unknown이나 다른 값의 경우
                }
            }

            return 0; // 지원하지 않는 provider

        } catch (NumberFormatException e) {
            // birthYear 파싱 실패시 기본값 0 반환
            return 0;
        }
    }


    // Oidc에서 왜 OAuthResponse를 받냐? => DTO에서 상속을 받기 때문
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== Google OIDC 로그인 시작 ===");

        // OIDC 프로토콜을 사용하는 구글 로그인 처리
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);

        System.out.println("Google OIDC attributes: " + oidcUser.getAttributes());

        // Google Response로 변환
        GoogleResponse oAuth2Response = new GoogleResponse(oidcUser.getAttributes());

        // 이메일로 기존 회원 확인
        String email = oAuth2Response.getUserEmail();
        System.out.println("이메일 체크: " + email);

        UsersEntity existingUser = usersRepository.findByUserEmail(email);

        if (existingUser != null) {
            if (existingUser.getUserSocial() != null) {
                return new CustomOIDCUsers(oAuth2Response, existingUser.getUserRole(), oidcUser,
                        oidcUser.getIdToken(), oidcUser.getUserInfo());
            } else {
                throw new OAuth2AuthenticationException("이미 일반 회원으로 가입된 이메일입니다. 일반 로그인을 이용해주세요.");
            }
        }

        // 신규 회원 가입 처리
        String userName = "google_" + oAuth2Response.getProviderId();
        UsersEntity existData = usersRepository.findByUserName(userName);
        UsersEntity.UserRole userRole = UsersEntity.UserRole.USER;

        if (existData == null) {
            UsersEntity usersEntity = new UsersEntity();
            usersEntity.setUserId(oAuth2Response.getUserEmail());
            usersEntity.setUserName(userName);
            usersEntity.setUserNickname(oAuth2Response.getUserName());
            usersEntity.setUserEmail(oAuth2Response.getUserEmail());
            usersEntity.setUserSocial(UsersEntity.UserSocial.GOOGLE);
            usersEntity.setUserRole(userRole);
            usersEntity.setUserStatus(UsersEntity.UserStatus.NORMAL);

            // birthDate 처리
            String birthDate = oAuth2Response.getBirthYear() + "-" + oAuth2Response.getBirthday();
            usersEntity.setBirthDate(birthDate);

            // gender 처리 - 수정된 컨버터 사용
            int gender = convertGender("google", oAuth2Response.getGender(), oAuth2Response.getBirthYear());
            usersEntity.setGender(gender);

            existData = usersRepository.save(usersEntity);

            CustomOIDCUsers customOIDCUsers = new CustomOIDCUsers(oAuth2Response, existData.getUserRole(), oidcUser,
                    oidcUser.getIdToken(), oidcUser.getUserInfo());
            customOIDCUsers.setUserId(oAuth2Response.getUserEmail());
            customOIDCUsers.setUserNickname(existData.getUserNickname());
        } else {
            userRole = existData.getUserRole();
        }

        CustomOIDCUsers customOIDCUsers = new CustomOIDCUsers(oAuth2Response, existData.getUserRole(), oidcUser,
                oidcUser.getIdToken(), oidcUser.getUserInfo());
        customOIDCUsers.setUserId(oAuth2Response.getUserEmail());  // 새로 생성된 userId 설정
        customOIDCUsers.setUserNickname(existData.getUserNickname());

        return customOIDCUsers;
    }
}
