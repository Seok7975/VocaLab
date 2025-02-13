package com.dev.vocalab.config;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOAuth2UsersService;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.oauth2.users.CustomOIDCUsersService;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.CustomUsersDetails;
import com.dev.vocalab.users.details.CustomUsersDetailsService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UsersService customOAuth2UsersService;
    private final CustomOIDCUsersService customOIDCUsersService;
    private final CustomUsersDetailsService customUsersDetailsService;
    private final UsersRepository usersRepository;

    public SecurityConfig(CustomOAuth2UsersService customOAuth2UsersService, CustomOIDCUsersService customOIDCUsersService, CustomUsersDetailsService customUsersDetailsService, UsersRepository usersRepository) {
        System.out.println("SecurityConfig 생성자 - customUsersDetailsService : " + customUsersDetailsService);
        System.out.println("SecurityConfig 생성자 - customOAuth2UsersService: " + customOAuth2UsersService);
        System.out.println("SecurityConfig 생성자 - customOIDCUsersService: " + customOIDCUsersService);
        this.customUsersDetailsService = customUsersDetailsService;
        this.customOIDCUsersService = customOIDCUsersService;
        this.customOAuth2UsersService = customOAuth2UsersService;
        this.usersRepository = usersRepository;
    }

    @PostConstruct
    public void init() {
        System.out.println("SecurityConfig 초기화 완료");
        System.out.println("CustomOAuth2UsersService 상태: " + customOAuth2UsersService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("SecurityConfig 초기화 - customOAuth2UsersService: " + customOAuth2UsersService);
        http.csrf((csrf) -> csrf.disable());

        http.formLogin(form -> form
                .loginPage("/login")                // 로그인 페이지 URL
                .loginProcessingUrl("/login-process")  // 로그인 처리 URL
                .usernameParameter("userId")        // 아이디 파라미터명
                .passwordParameter("userPassword")   // 비밀번호 파라미터명
                .defaultSuccessUrl("/main", true)         // 로그인 성공 시 이동
                .successHandler((request, response, authentication) -> {
                    // 로그인 성공 로깅
                    System.out.println("일반 로그인 성공: " + authentication.getName());
                    if (authentication.getPrincipal() instanceof CustomUsersDetails) {
                        CustomUsersDetails userDetails = (CustomUsersDetails) authentication.getPrincipal();
                        System.out.println("로그인 사용자 정보: " + userDetails.getUsername());
                    }
                    response.sendRedirect("/main");
                })
                // 로직
                .failureHandler((request, response, exception) -> {
                    // 로그인 실패 시 처리
                    System.out.println("로그인 실패: " + exception.getMessage());

                    String errorMessage = "";

                    if (exception instanceof UsernameNotFoundException) {
                        errorMessage = URLEncoder.encode("존재하지 않는 아이디입니다.", "UTF-8");
                    } else if (exception instanceof BadCredentialsException) {
                        errorMessage = URLEncoder.encode("올바르지 않은 비밀번호입니다.", "UTF-8");
                    } else {
                        errorMessage = URLEncoder.encode("로그인에 실패했습니다.", "UTF-8");
                    }

                    response.sendRedirect("/login?error=true&message=" + errorMessage);
                })
                //.failureUrl("/login?error") // 얘는 간단한 리다이렉트만
                .permitAll());
        http.userDetailsService(customUsersDetailsService).csrf(csrf -> csrf.disable());

        http.httpBasic((basic) -> basic.disable());

        http.oauth2Login(oauth2 ->
                oauth2.loginPage("/login")
//                        .clientRegistrationRepository(customClientRegistrationRepo.clientRegistrationRepository())
//                        .authorizedClientService(customOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(jdbcTemplate, customClientRegistrationRepo.clientRegistrationRepository()))
                        .userInfoEndpoint(userInfo -> {
                            System.out.println("OAuth2 로그인 - 사용자 서비스 진입");
                            userInfo.userService(customOAuth2UsersService) // OAuth2 서비스(네이버)
                                    .oidcUserService(customOIDCUsersService); // OIDC 서비스(구글)
                            //userInfo.oidcUserService(customOAuth2UsersService::loadOidcUser);
                        })
                        .successHandler((request, response, authentication) -> {
                            System.out.println("OAuth2 로그인 성공: " + authentication.getName());
                            System.out.println("Authentication details: " + authentication.getDetails());

                            // Principal에서 nickname 확인
                            if (authentication.getPrincipal() instanceof CustomOAuth2Users) {
                                CustomOAuth2Users oAuth2Users = (CustomOAuth2Users) authentication.getPrincipal();
                                System.out.println("서비스명: " + oAuth2Users.getOAuth2Response().getProvider());
                                System.out.println("Nickname: " + oAuth2Users.getUserNickname());
                            } else if (authentication.getPrincipal() instanceof CustomOIDCUsers) {
                                CustomOIDCUsers oidcUsers = (CustomOIDCUsers) authentication.getPrincipal();
                                System.out.println("OIDC 서비스명: " + oidcUsers.getOAuth2Response().getProvider());
                                System.out.println("Nickname: " + oidcUsers.getUserNickname());
                            } else {
                                System.out.println("알 수 없는 인증 타입: " + authentication.getPrincipal().getClass().getName());
                            }

                            response.sendRedirect("/main");
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("OAuth2 로그인 실패: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("/login?error=oauth2");
                        })
        );


// true를 추가하여 항상 이 URL로 리다이렉트);

        http.authorizeHttpRequests((authorize) ->
                authorize.requestMatchers("/**").permitAll()
        );

        http.logout(logout -> logout
                .logoutSuccessHandler(new CustomLogoutSuccessHandler()) // 커스텀 로그아웃 핸들러 사용
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );

        return http.build();
    }

    // 로그인과정에서 /login-process를 거치지 않고 수행. 인증과정에서 예외가 발생할 시 failureHandler 호출
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider() {
            @Override
            protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                          UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
                if (authentication.getCredentials() == null) {
                    throw new BadCredentialsException("비밀번호를 입력해주세요.");
                }

                String presentedPassword = authentication.getCredentials().toString();
                if (!passwordEncoder().matches(presentedPassword, userDetails.getPassword())) {
                    throw new BadCredentialsException("올바르지 않은 비밀번호입니다.");
                }
            }
        };

        provider.setUserDetailsService(customUsersDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);  // 이 설정이 중요합니다!
        return provider;
    }

    // 커스텀 로그아웃 성공 핸들러
    private static class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
            // OAuth2 로그인 사용자인 경우
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

                // OAuth2 제공자별 로그아웃 URL
//                String logoutUrl = switch (registrationId) {
//                    case "google" -> "https://accounts.google.com/Logout";
//                    case "naver" -> "https://nid.naver.com/nidlogin.logout";
//                    default -> "/login";
//                };

                // 세션 무효화
                request.getSession().invalidate();

                // OAuth2 제공자의 로그아웃 페이지로 리다이렉트
//                response.sendRedirect(logoutUrl);
                response.sendRedirect("/login");
            } else {
                // 일반 로그인 사용자인 경우
                response.sendRedirect("/login");
            }
        }
    }

    // 해싱 DI
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
