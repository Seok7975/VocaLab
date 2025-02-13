package com.dev.vocalab.oauth2.users;

import com.dev.vocalab.users.UsersEntity;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;

// 구글용 OAuth2 분리, DTO
public class CustomOIDCUsers extends CustomOAuth2Users implements OidcUser {

    private final OidcUser oidcUser;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomOIDCUsers(OAuth2Response oAuth2Response, UsersEntity.UserRole userRole, OidcUser oidcUser,
                           OidcIdToken idToken, OidcUserInfo userInfo) {
        super(oAuth2Response, userRole);
        this.oidcUser = oidcUser;
        this.idToken = idToken;
        this.userInfo = userInfo;
        this.setUserNickname(oAuth2Response.getUserName());
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }
}