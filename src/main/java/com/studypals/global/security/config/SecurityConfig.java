package com.studypals.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.global.security.jwt.JwtAuthenticationFilter;
import com.studypals.global.security.jwt.JwtUtils;

/**
 * spring security 의 config 클래스입니다.
 *
 * <p>JWT 인증을 위한 무상태/web security/Jwt filter 를 등록하였습니다.
 *
 * <p><b>빈 관리:</b><br>
 * ObjectMapper 를 주입받아 {@link JwtAuthenticationFilter} 를 생성합니다.
 *
 * @author jack8
 * @see JwtAuthenticationFilter
 * @see AccessURL
 * @see JwtUtils
 * @since 2025-04-02
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtils jwtUtils) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        (auth) -> {
                            AccessURL.PUBLIC
                                    .getUrls()
                                    .forEach(url -> auth.requestMatchers(url).permitAll());
                            auth.anyRequest().authenticated();
                        })
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtils, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
