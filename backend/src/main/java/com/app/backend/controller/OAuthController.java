package com.app.backend.controller;

import com.app.backend.controller.dto.TokenDto;
import com.app.backend.service.auth.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth/*")
@Slf4j
public class OAuthController {


    private final AuthServiceImpl authServiceImpl;

    private static final String REDIRECT_URI = "http://kareer.cloud/oauth";

    @GetMapping("loginInfo")
    public void onAuthenticationSuccess(HttpServletResponse response,
                                        @RequestParam String email,
                                        @RequestParam String name,
                                        @RequestParam String phone,
                                        @RequestParam String profile) throws IOException {
        // 토큰 발행
        TokenDto tokenDto = authServiceImpl.socialLogin(email, name, phone);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        Long accessTokenExpiresIn = tokenDto.getAccessTokenExpiresIn();


        // 토큰 정보를 포함하여 리다이렉트
        String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("accessTokenExpiresIn", accessTokenExpiresIn)
                .queryParam("profile", profile)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
