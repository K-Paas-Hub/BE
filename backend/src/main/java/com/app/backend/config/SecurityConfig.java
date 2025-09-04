package com.app.backend.config;

import com.app.backend.entity.type.Authority;
import com.app.backend.handler.MyAuthenticationSuccessHandler;
import com.app.backend.jwt.JwtAccessDeniedHandler;
import com.app.backend.jwt.JwtAuthenticationEntryPoint;
import com.app.backend.jwt.TokenProvider;
import com.app.backend.service.MemberDetailService;
import com.app.backend.service.oauth.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private static final String AUTH_PATH = "/auth/**";
    private static final String OAUTH_PATH = "/oauth/**";
    private static final String MEMBER_PATH = "/member/**";
    private static final String ADMIN_PATH = "/admin/**";
    private static final String WEBSOCKET_PATH = "/ws/**";
    private static final String FILE_PATH = "/file/**";
    private static final String FILES_PATH = "/files/**";
    private static final String OPENAI_PATH = "/OpenAI/**";

    private final OAuthService oAuthService;
    private final MemberDetailService memberDetailService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .userDetailsService(memberDetailService)

                // CORS 설정 (람다식 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // Exception Handling 설정
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )


                // 세션을 사용하지 않도록 Stateless 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 권한 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(AUTH_PATH).permitAll()
                        .requestMatchers(OAUTH_PATH).permitAll()
                        .requestMatchers("test").permitAll()
                        .requestMatchers("kakao").permitAll()
                        .requestMatchers(MEMBER_PATH).hasRole(Authority.USER.name())
                        .requestMatchers(ADMIN_PATH).hasRole(Authority.ADMIN.name())
                        .anyRequest().authenticated()
                )

                // JWT 필터 적용 (with() 메서드 사용)
                .with(new JwtSecurityConfig(tokenProvider), customizer -> {})

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(new MyAuthenticationSuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuthService)
                        )
                )

                .build();
    }

    // CORS 설정을 위한 별도 Bean (필요한 경우)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return (CorsConfigurationSource) source;
    }
}

// JwtSecurityConfig도 업데이트가 필요할 수 있습니다
/*
@RequiredArgsConstructor
public class JwtSecurityConfig extends AbstractHttpConfigurer<JwtSecurityConfig, HttpSecurity> {
    private final TokenProvider tokenProvider;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
*/