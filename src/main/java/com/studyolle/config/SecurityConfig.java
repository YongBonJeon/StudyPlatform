package com.studyolle.config;


import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 정적 요소 무시
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 해당 링크들을 제외한 모든 요청은 인증을 받아야 한다.
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                                "/email-login", "/check-email-login", "/login-link", "/images/**",
                                "/css/**", "/js/**", "/node_modules/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated());

        // 로그인 페이지 설정
        http.formLogin(formLogin -> formLogin
                .loginPage("/login").permitAll());

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutSuccessUrl("/"));

        return http.build();
    }
}
