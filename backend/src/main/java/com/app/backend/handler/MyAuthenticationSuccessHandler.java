package com.app.backend.handler;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {

            OAuth2User oAuth2User = (OAuth2User) principal;
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String phoneNum = (String) kakaoAccount.get("phone_number");
            phoneNum = phoneNum.replace(" ","").replace("-","");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String profileImageUrl = (String) profile.get("profile_image_url");
            log.info("phoneNum : " + phoneNum+", profileUrl : "+profileImageUrl);

            System.out.println("SuccessHandler oAuth2User: " + oAuth2User);
            log.info("name = "+name);
            String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                    .queryParam("email", email)
                    .queryParam("name",name)
                    .queryParam("phone",phoneNum)
                    .queryParam("profile",profileImageUrl)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            response.sendRedirect(redirectUrl);
        } else {
            throw new IllegalStateException("Authentication principal is not an instance of OAuth2User");
        }
    }
}