package com.back.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/favicon.ico").permitAll()
                                .requestMatchers("/resource/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                // 042-B 나아가기 : 글·댓글 작성은 로그인 필요
                                .requestMatchers("/posts/write").authenticated()
                                .requestMatchers("/posts/*/comments/write").authenticated()
                                // 043-A 나아가기 : 수정·삭제도 로그인 필요 (작성자 확인은 컨트롤러에서)
                                .requestMatchers("/posts/*/modify", "/posts/*/delete").authenticated()
                                .requestMatchers("/posts/*/comments/*/modify", "/posts/*/comments/*/delete").authenticated()
                                // 044 나아가기 : 추천은 로그인 필요
                                .requestMatchers("/posts/*/vote", "/posts/*/comments/*/vote").authenticated()
                                .requestMatchers("/**").permitAll() // 추후 상황에 맡게 변경
                                .anyRequest().authenticated()
                )
                .headers(
                        headers -> headers
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .csrf(
                        (csrf) -> csrf
                                .ignoringRequestMatchers("/h2-console/**")
                )
                // 041-B 나아가기 : 로그인/로그아웃
                .formLogin(
                        form -> form
                                .loginPage("/members/login")
                                .defaultSuccessUrl("/posts")
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/members/logout")
                                .logoutSuccessUrl("/posts")
                );

        return http.build();
    }

    // 041-A 나아가기 : 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}