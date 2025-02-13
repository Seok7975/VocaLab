package com.dev.vocalab.users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@ToString
@Data
@Table(name = "Users")
public class UsersEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;

    @Column(name = "userId", length = 100, nullable = false, unique = true)
    private String userId;

    @Column(name = "userName", length = 100, nullable = false)
    private String userName;

    @Column(name = "userPassword", length = 255)
    private String userPassword;

    @Column(name = "userNickname", length = 100, nullable = false, unique = true)
    private String userNickname;

    @Column(name = "userEmail", length = 100, nullable = false, unique = true)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "userSocial", nullable = false)
    private UserSocial userSocial = UserSocial.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "userRole", nullable = false)
    private UserRole userRole = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "userStatus", nullable = false)
    private UserStatus userStatus = UserStatus.NORMAL;

    @Column(name = "birthDate", length = 10, nullable = false)
    private String birthDate;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    // Hibernate가 엔티티가 처음 저장될 때 자동으로 현재 시간 설정
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 엔티티에 정의된 @PrePersist와 @PreUpdate 메서드에 의해 자동으로 관리
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public enum UserSocial {
        NONE, GOOGLE, NAVER
    }

    public enum UserRole {
        USER, ADMIN
    }

    public enum UserStatus {
        NORMAL, BANNED
    }

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

