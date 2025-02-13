package com.dev.vocalab.users;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 이메일 인증 과정에서 사용되는 DTO
@Getter
@AllArgsConstructor
public class VerificationInfo {
    private String userEmail; // 인증을 요청한 사용자의 이메일
    private String code; // 생성된 인증 코드
    private LocalDateTime expireTime; // 인증 코드의 만료 시간

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
