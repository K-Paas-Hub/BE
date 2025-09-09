package com.app.backend.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REDIRECT_URI = "/oauth/loginInfo";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalStateException("Authentication is not OAuth2AuthenticationToken");
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // google or kakao
        OAuth2User oAuth2User = (OAuth2User) oauthToken.getPrincipal();

        String email = null;
        String name = null;
        String phoneNum = null;
        String profileImageUrl = null;

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    name = (String) profile.get("nickname");
                    profileImageUrl = (String) profile.get("profile_image_url");
                }
                phoneNum = (String) kakaoAccount.get("phone_number");
                if (phoneNum != null) {
                    phoneNum = phoneNum.replace(" ", "").replace("-", "");
                }
            }
        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            profileImageUrl = (String) attributes.get("picture");
            // 구글은 phone_number 기본 scope엔 없음 → null 유지됨
        }

        log.info("provider={}, email={}, name={}, phone={}, profileUrl={}",
                registrationId, email, name, phoneNum, profileImageUrl);

        String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("email", email)
                .queryParam("name", name)
                .queryParam("phone", phoneNum)
                .queryParam("profile", profileImageUrl)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
