package com.dev.vocalab.users.details;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

public class AuthenticationUtil {
    // 모델에 현재 로그인된 사용자 정보를 추가하는 메서드
    // JSP에서 userSession으로 접근 가능
    public static void addUserSessionToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                model.addAttribute("userSession", principal);
            } else if (principal instanceof CustomOAuth2Users) {
                CustomOAuth2Users oauth2User = (CustomOAuth2Users) principal;
                model.addAttribute("userSession", oauth2User);
            } else if (principal instanceof CustomOIDCUsers) {
                CustomOIDCUsers oidcUser = (CustomOIDCUsers) principal;
                model.addAttribute("userSession", oidcUser);
            }
        }
    }

    // 현재 로그인된 사용자의 CustomUsersDetails 객체를 반환하는 메서드
    // 일반 로그인 사용자만 해당됨 (OAuth2, OIDC 사용자는 null 반환)
    public static CustomUsersDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                return (CustomUsersDetails) principal;
            }
        }
        return null;
    }

    // 현재 로그인된 사용자의 ID를 반환하는 메서드
    // 모든 인증 방식(일반 로그인, OAuth2, OIDC)에 대해 동작
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                return ((CustomUsersDetails) principal).getUserId();
            } else if (principal instanceof CustomOIDCUsers) {
                return ((CustomOIDCUsers) principal).getUserId();
            } else if (principal instanceof CustomOAuth2Users) {
                return ((CustomOAuth2Users) principal).getUserId();
            }
        }
        return null;
    }

    // 사용자가 인증되었는지 확인하는 메서드
    // anonymousUser도 체크하여 더 정확한 인증 상태 확인
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    // 현재 로그인된 사용자의 역할(Role)을 반환하는 메서드
    // OAuth2와 OIDC 사용자는 기본적으로 USER 역할 할당
    public static CustomUsersDetails.UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                return ((CustomUsersDetails) principal).getUserRole();
            } else if (principal instanceof CustomOIDCUsers || principal instanceof CustomOAuth2Users) {
                return CustomUsersDetails.UserRole.USER;
            }
        }
        return null;
    }
}
