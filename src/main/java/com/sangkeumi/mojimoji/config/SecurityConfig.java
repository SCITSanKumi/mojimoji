package com.sangkeumi.mojimoji.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sangkeumi.mojimoji.handler.LoginFailureHandler;
import com.sangkeumi.mojimoji.handler.LoginSuccessHandler;
import com.sangkeumi.mojimoji.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig
 *
 * Spring Security의 보안 정책을 설정하는 클래스.
 * - HTTP 요청 권한 부여
 * - OAuth2 소셜 로그인
 * - Form 로그인
 * - 로그아웃
 * - CSRF 설정 등
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 로그인 성공/실패 시 추가 로직을 처리할 핸들러
     */
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    /**
     * 소셜 로그인 시, 사용자 정보를 가져와 DB와 연동하는 커스텀 OAuth2UserService
     */
    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * Spring Security의 필터 체인(보안 설정)을 구성하는 메서드.
     *
     * @param http HttpSecurity 객체 (Security 설정의 핵심 API)
     * @return SecurityFilterChain (스프링 시큐리티가 사용하는 필터 체인)
     * @throws Exception 예외 발생 시
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1) HTTP 요청에 대한 보안 설정
        http.authorizeHttpRequests(auth -> auth
                // requestMatchers(...)로 지정한 경로들은 인증 없이 접근 허용
                .requestMatchers(
                        "/",
                        "/board/story/list",
                        "/board/story/ajaxList",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/user/login",
                        "/user/regist",
                        "/user/id-check",
                        "/user/nickname-check",
                        "/user/email-check",
                        "/user/sign-up",
                        "/user/sign-in",
                        "/js/**",
                        "/fonts/**",
                        "/css/**",
                        "/image/**")
                .permitAll()
                // 그 외 모든 요청은 인증(로그인) 필요
                .anyRequest().authenticated())

                // 2) Form 로그인 설정
                .formLogin(form -> form
                        // 로그인 페이지 경로 (GET /user/login) → 로그인 폼 표시
                        .loginPage("/user/login")
                        // 로그인 처리 경로 (POST /user/sign-in) → 실제 인증 로직
                        .loginProcessingUrl("/user/sign-in")
                        // 로그인 성공 시 커스텀 핸들러
                        .successHandler(loginSuccessHandler)
                        // 로그인 실패 시 커스텀 핸들러
                        .failureHandler(loginFailureHandler)
                        // 로그인 폼에서 아이디/비번 input name 지정
                        .usernameParameter("username")
                        .passwordParameter("password")
                        // 로그인 관련 요청은 모두 접근 허용
                        .permitAll())
                // 3) 소셜 로그인(OAuth2) 설정
                .oauth2Login(oauth -> oauth
                        // userInfoEndpoint(): 소셜 인증 후 사용자 정보(UserInfo)를 가져오는 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                // userService(...)에 커스텀 OAuth2UserService를 등록
                                // → 소셜 프로필(DB 저장 등) 커스텀 로직
                                .userService(customOAuth2UserService))
                        // 로그인 성공 시 이동할 URL, true면 항상 해당 URL로 이동
                        .defaultSuccessUrl("/", true))

                // 4) 로그아웃 설정
                .logout(logout -> logout
                        // 로그아웃 요청 경로
                        .logoutUrl("/user/logout")
                        // 로그아웃 성공 시 이동할 경로
                        .logoutSuccessUrl("/")
                        // 로그아웃 시 세션 무효화
                        .invalidateHttpSession(true)
                        // 인증 정보 제거
                        .clearAuthentication(true));

        // 5) CSRF 설정 (개발 편의를 위해 disable)
        http.csrf(csrf -> csrf.disable());

        // 6) SecurityFilterChain 객체 빌드 및 반환
        return http.build();
    }

    /**
     * BCryptPasswordEncoder
     *
     * 비밀번호를 안전하게 저장하기 위한 단방향 암호화(해싱) 객체.
     * 스프링 시큐리티에서 회원가입 시 비밀번호를 인코딩하거나,
     * 로그인 시 비밀번호 매칭을 위해 사용.
     */
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
