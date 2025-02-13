package com.dev.vocalab.oauth2.users;

// 데이터를 받을 수 있는 바구니
public interface OAuth2Response {

    String getProvider(); // 서비스 제공자 이름

    String getProviderId(); // 제공 받는자 번호

    String getUserId();

    String getUserEmail(); // 이메일
    String getUserName(); // 사용자 이름

    String getUserNickname();

    String getGender();       // 성별
    String getBirthday();     // 생일
    String getBirthYear();    // 출생년도
}
