package com.dev.vocalab.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// UsersInformation(데이터 값 전달용, 변환 파일)
@Getter
@Setter
public class UsersDTO {
    private String loginType;
    private String userId;
    private String userName;
    private String userNickname;
}

