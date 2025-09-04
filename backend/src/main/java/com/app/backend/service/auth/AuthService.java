package com.app.backend.service.auth;

import com.app.backend.controller.dto.TokenDto;
import com.app.backend.controller.dto.TokenRequestDto;
import com.app.backend.entity.Member;

import java.util.Optional;

public interface AuthService {
    public Optional<Member> getMemberByEmail(String email);
    public TokenDto socialLogin(String email, String name, String phone);
    public TokenDto signupAndLogin(String email, String name, String phone);
    public TokenDto updateAndLogin(Member member, String name, String phone);
    public TokenDto reissue(TokenRequestDto tokenRequestDto);
}
