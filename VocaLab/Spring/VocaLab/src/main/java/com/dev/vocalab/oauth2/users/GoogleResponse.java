package com.dev.vocalab.oauth2.users;

import java.util.Map;

// DTO
public class GoogleResponse implements OAuth2Response{

    // json 데이터를 받을 Map형식의 변수 선언
    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString(); // 구글은 id가아닌 sub이 키워드
    }

    @Override
    public String getUserId() {
        return attribute.get("id").toString();
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

    // 구글에서 제공하지 않을 가능성 있는 컬럼은 getOrDefault 사용
    @Override
    public String getGender() {
        return attribute.getOrDefault("gender", "unknown").toString();
        // return attribute.get("gender").toString();
    }

    @Override
    public String getBirthday() {
        return attribute.getOrDefault("birthday", "00-00").toString();
        // return attribute.get("birthday").toString();
    }

    @Override
    public String getBirthYear() {
        return attribute.getOrDefault("birthYear", "0000").toString();
        // return attribute.get("birthYear").toString();
    }
}