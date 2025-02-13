package com.dev.vocalab.oauth2.users;

import com.dev.vocalab.users.UsersEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
// OAuth2 인증 과정에서 사용되는 사용자 정보를 담는 어댑터(DTO)
public class CustomOAuth2Users implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final UsersEntity.UserRole userRole; // enum이므로

    private String userId;

    private String userNickname; // 닉네임 필드 추가



    public CustomOAuth2Users(OAuth2Response oAuth2Response, UsersEntity.UserRole userRole) {
        this.oAuth2Response = oAuth2Response;
        this.userId = oAuth2Response.getUserEmail();
        this.userRole = userRole;
    }


    @Override
    public Map<String, Object> getAttributes() {
        // 리소스 서버로부터 오는 모든 데이터
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //롤값을 정의
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return "ROLE_" + userRole.name();
            }
        });

        return collection;
    }

    public String getUserId() {
        return userId != null ? userId : oAuth2Response.getUserEmail();
    }

    // 사용자 별명, 이름값
    @Override
    public String getName() {
        return oAuth2Response.getUserName();
    }

    // nickname이 null이면 OAuth2Response의 userName을 반환 => 이게 지금까지 없어서 null을 계속 반환한거였네
    public String getUserNickname() {
        return userNickname != null ? userNickname : oAuth2Response.getUserName();
    }

    // 우리가 전달받은 소셜로그인 데이터는 userName이라 지칭할게 없음
    // 아이디값을 Provider에, 발급받은 코드를 ProviderId로 아이디를 만듬
    public String getUserName() {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }


}
