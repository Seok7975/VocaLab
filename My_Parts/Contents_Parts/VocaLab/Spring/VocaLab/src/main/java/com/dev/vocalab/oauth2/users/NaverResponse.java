package com.dev.vocalab.oauth2.users;

import java.util.Map;

// DTO
public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getUserId() {
        return (String) attribute.get("id");
    }

    @Override
    public String getUserEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getUserName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getUserNickname() {
        return attribute.get("nickname").toString();
    }

    @Override
    public String getGender() {
        return attribute.get("gender").toString();
    }

    @Override
    public String getBirthday() {
        return attribute.get("birthday").toString();
    }

    @Override
    public String getBirthYear() {
        return attribute.get("birthyear").toString();
    }


}
