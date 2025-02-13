package com.dev.vocalab.users.details;

import com.dev.vocalab.users.UsersEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// Spring Security 인증을 위한 UserDetails(인증용 객체)
// @Data보다는 상속받고있기에 직접 Getter Setter 만드는 것이 낫다.
@Getter
@Setter
public class CustomUsersDetails extends User {

    private String userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String birthDate;
    private Integer gender;
    private UserSocial userSocial;
    private UserRole userRole;
    private UserStatus userStatus;


    // UsersEntity를 받는 생성자 추가
    public CustomUsersDetails(UsersEntity user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUserId(), user.getUserPassword(), authorities);

        // 추가 필드들 설정
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userNickname = user.getUserNickname();
        this.userEmail = user.getUserEmail();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        // enum 설정 (실제 UsersEntity의 필드에 따라 적절히 변환)
        this.userSocial = UserSocial.valueOf(user.getUserSocial().name()); // 일반 로그인의 경우
        this.userRole = UserRole.valueOf(user.getUserRole().name()); // 기본값, 실제 역할에 따라 설정
        this.userStatus = UserStatus.valueOf(user.getUserStatus().name()); // 기본값
    }

    public CustomUsersDetails(String userId, String userPassword, Collection<? extends GrantedAuthority> authorities) {
        super(userId, userPassword, authorities);
    }

    // enum 정의
    public enum UserSocial {
        NONE, GOOGLE, NAVER
    }

    public enum UserRole {
        USER, ADMIN
    }

    public enum UserStatus {
        NORMAL, BANNED
    }

    // OAuth2User와의 일관성을 위해 추가
    public String getName() {
        return this.userNickname != null ? this.userNickname : this.userName;
    }

}
