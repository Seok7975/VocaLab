package com.dev.vocalab.oauth2.users;

import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UsersService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    //DefaultOAuth2UserService는 OAuth2UserService의 구현체이기 때문에 상속받음

    private final UsersRepository usersRepository;

    public CustomOAuth2UsersService(UsersRepository usersRepository) {
        System.out.println("CustomOAuth2UsersService(Naver) 빈 등록 완료");
        this.usersRepository = usersRepository;
    }

    // 네이버 전용 불러들인 gender값 변환 메서드
    private int convertGender(String provider, String userGender, String birthYear) {
        try {
            int year = Integer.parseInt(birthYear);
            boolean isBornAfter2000 = year >= 2000;

            if ("naver".equals(provider)) {
                if ("M".equals(userGender)) {
                    return isBornAfter2000 ? 3 : 1; // 2000년대 남자: 3, 1900년대 남자: 1
                } else if ("F".equals(userGender)) {
                    return isBornAfter2000 ? 4 : 2; // 2000년대 여자: 4, 1900년대 여자: 2
                }
            }

            throw new IllegalArgumentException("성별이나 출생연도 확인이 불가합니다.");

        } catch (NumberFormatException e) {
            // birthYear가 숫자로 변환할 수 없는 경우
            throw new IllegalArgumentException("유효하지 않은 데이터 타입(숫자로 변환 불가)");
        }
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // userRequest인자를 통해 구글 네이버 인증 데이터가 넘어옴

        System.out.println("=== OAuth2 로그인 시작 ===");
        System.out.println("Provider: " + userRequest.getClientRegistration().getRegistrationId());
        System.out.println("Access Token: " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest); // 부모클래스에 존재하는 loadUser메서드에 userRequest를 넣고 유저정보를 가져옴
        System.out.println("OAuth2User attributes: " + oAuth2User.getAttributes()); // 가져온 OAuth2User 데이터 로깅

        //1 . OAuth 제공자 확인
        // 넘어온 데이터가 어느 인증 서비스 provider인지 알기위해
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null; // 네이버 구글 인증 규격이 다르기 때문에 이를 따로 받을 DTO를 생성해야함
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes()); // 네이버 방식대로 oAuth2Response 바구니에 getAttributes로 데이터를 꺼내 넣음
            System.out.println("Naver OAuth attributes: " + oAuth2User.getAttributes());
        }
//        else if (registrationId.equals("google")) {
//
//            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes()); // 구글 방식대로 oAuth2Response 바구니에 getAttributes로 데이터를 꺼내 넣음
//            System.out.println("Google OAuth attributes: " + oAuth2User.getAttributes());
//        }
        else {
            System.out.println("지원하지 않는 소셜 로그인입니다.");
            return null;
        }

        // 2. 이메일로 기존 회원 확인
        String email = oAuth2Response.getUserEmail();
        System.out.println(" 이메일 체크: " + email);

        UsersEntity existingUser = usersRepository.findByUserEmail(email);

        if (existingUser != null) {
            // 3a. 기존 회원인 경우
            if (existingUser.getUserSocial() != null) {
                // 소셜 로그인으로 가입한 계정인 경우
                return new CustomOAuth2Users(oAuth2Response, existingUser.getUserRole());
            } else {
                // 일반 회원가입으로 가입한 계정인 경우
                throw new OAuth2AuthenticationException("이미 일반 회원으로 가입된 이메일입니다. 일반 로그인을 이용해주세요.");
            }
        }

        // 3. 신규 회원 가입 처리
        String userName = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        UsersEntity existData = usersRepository.findByUserName(userName);

//        String userRole = "ROLE_USER";
        // UserRole enum 직접 사용
        UsersEntity.UserRole userRole = UsersEntity.UserRole.USER;

        // 처음 로그인(신규 사용자)
        if (existData == null) {
            UsersEntity usersEntity = new UsersEntity();
            usersEntity.setUserId(oAuth2Response.getUserEmail());
            usersEntity.setUserName(userName);
            usersEntity.setUserNickname(oAuth2Response.getUserName()); // 실제 이름을 닉네임으로 설정
            //usersEntity.setUserNickname(userName); // 임시로 이름을 닉네임으로, 후에 사용자가 수정하도록
            usersEntity.setUserEmail(oAuth2Response.getUserEmail());

            // 소셜 로그인 제공자 설정
            UsersEntity.UserSocial userSocial = UsersEntity.UserSocial.NAVER;
            usersEntity.setUserSocial(userSocial);

            usersEntity.setUserRole(userRole);
            usersEntity.setUserStatus(UsersEntity.UserStatus.NORMAL);

            // birthDate 처리
            String birthDate = oAuth2Response.getBirthYear() + "-" + oAuth2Response.getBirthday();
            usersEntity.setBirthDate(birthDate);

            // gender 처리
            int gender = convertGender(oAuth2Response.getProvider(), oAuth2Response.getGender(), oAuth2Response.getBirthYear());
            usersEntity.setGender(gender);

            // 생성일/수정일 설정 => 엔티티에서 알아서 처리
//            LocalDateTime now = LocalDateTime.now();
//            usersEntity.setCreatedAt(now);
//            usersEntity.setUpdatedAt(now);

            existData = usersRepository.save(usersEntity);

            CustomOAuth2Users customOAuth2Users = new CustomOAuth2Users(oAuth2Response, existData.getUserRole());
            customOAuth2Users.setUserId(oAuth2Response.getUserEmail());  // 새로 생성된 userId 설정
            customOAuth2Users.setUserNickname(existData.getUserNickname());

            return customOAuth2Users;

        } else { // 이미 존재하는
            userRole = existData.getUserRole();
        }

        CustomOAuth2Users customOAuth2Users = new CustomOAuth2Users(oAuth2Response, existData.getUserRole());
        customOAuth2Users.setUserId(existData.getUserId());
        String nickname = existData.getUserNickname();
        System.out.println("Setting nickname for user: " + nickname); // 디버깅 로그
        customOAuth2Users.setUserNickname(nickname); // 닉네임 설정
        System.out.println("Nickname after setting: " + customOAuth2Users.getUserNickname()); // 확인 로그

        return customOAuth2Users;
    }

}







//
//    // 업데이트
//            existData.setUserName(userName);
//            existData.setUserEmail(oAuth2Response.getUserEmail());
//
//    // 기존 사용자의 role을 사용
//    userRole = existData.getUserRole();
//
//            usersRepository.save(existData);
//}
//
//        return new CustomOAuth2Users(oAuth2Response, userRole); // 쿼리에는 이넘을 해놓긴했는데 좀따 염두해야할듯
